package com.joyent.manta.kafka;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObjectResponse;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;


/**
 * This class exposes two public methods, <code>put</code> and <code>flush</code>,
 * to manage the temporary file for the Kafka message aggregation, and post it
 * to Manta if (1) the file size is sufficiently large, or (2) the number of
 * messages is enough to publish.
 */
public class MantaWriter {
    private static final Logger LOG = LoggerFactory.getLogger(MantaWriter.class);

    private ObjectFactory factory;
    // TODO: we may need a Map[Topic, LocalObjectWriter].
    private LocalObjectWriter fileWriter;
    /**
     * Manta pathname, will be used for when fileWriter is posted.
     */
    private MantaPathname mantaPathname;

    private String objectPattern;
    private String objectClass;
    private MantaClient manta;

    /**
     * FirstRecord serves two purposes - If this is null, then MantaWriter will
     * consider that as a signal for initializing internal states. If not, it is
     * used as a source of MantaPathname parameters.
     */
    private SinkRecord firstRecord;

    /**
     * The number of records in the current temporary file.
     */
    private long objectCount;

    /**
     * The byte size of the temporary file.
     */
    private long objectSize;

    private String mantaShouldFail;

    public MantaWriter(final MantaClient mantaClient,
                       final Map<String, String> context) {
        this(mantaClient, context, new ObjectFactory());
    }

    public MantaWriter(final MantaClient mantaClient,
                       final Map<String, String> context,
                       final ObjectFactory factory) {
        this.factory = factory;

        this.manta = mantaClient;
        this.objectPattern = context.get(MantaSinkConfigDef.MANTA_OBJECT_PATTERN);
        this.objectClass = context.get(MantaSinkConfigDef.MANTA_OBJECT_CLASS);

        this.objectCount = Long.parseLong(context.getOrDefault(
                MantaSinkConfigDef.MANTA_OBJECT_LIMIT_COUNT, "-1"));
        this.objectSize = Long.parseLong(context.getOrDefault(
                MantaSinkConfigDef.MANTA_OBJECT_LIMIT_SIZE, "-1"));
        this.mantaShouldFail = context.get(MantaSinkConfigDef.MANTA_SIMULATE_FAILURE);
    }

    void closeWriter() {
        if (fileWriter == null) {
            return;
        }

        try {
            fileWriter.delete();
            fileWriter.close();
        } catch (IOException e) {
            LOG.warn("closing fileWriter failed", e);
        } finally {
            fileWriter = null;
        }
    }

    void openLocalChunkIfNotExist(final SinkRecord firstRecord) throws IOException {
        if (this.firstRecord != null) {
            return;
        }

        try { // Generate MantaPathname using <code>firstRecord</code>

            this.firstRecord = firstRecord;
            fileWriter = factory.getObject(LocalObjectWriter.class,
                    new LocalObjectWriter(objectClass));
            final MantaPathname pathname = new MantaPathname(manta.getContext(),
                    objectPattern, firstRecord);
            mantaPathname = factory.getObject(MantaPathname.class, pathname);

            LOG.info("TEMP[#%d]: {}", firstRecord.kafkaPartition(), fileWriter.getPath());
        } catch (Exception e) {
            this.firstRecord = null;

            closeWriter();
            throw e;
        }
    }

    // Send the records in the temporary file to Manta, then delete the temporary file.
    void closeLocalChunk() throws IOException {
        try {
            fileWriter.close();

            manta.putDirectory(mantaPathname.getDirectory(), true);

            try (FileInputStream fs = new FileInputStream(fileWriter.getPath());
                 BufferedInputStream bs = new BufferedInputStream(fs);
                 InputStream is = factory.getObject(BufferedInputStream.class, bs)) {

                if (!mantaShouldFail.isEmpty() && Files.exists(Paths.get(mantaShouldFail))) {
                    throw new IOException("Simulating Manta client exception.");
                }

                MantaObjectResponse resp = manta.put(mantaPathname.toString(),
                        is, fileWriter.getSize(), null, null);
                LOG.info("manta resp: {}", resp);
            }
        } catch (IOException e) {
            // ignored
            LOG.error(String.format("IOException on MantaClient: %s", e.getMessage()), e);
            throw e;
        } catch (Exception e) {
            LOG.error("uncatched exception", e);
            throw e;
        } finally {
            firstRecord = null;
            closeWriter();
        }
    }

    public void flush() throws IOException {
        // Called by Kafka Connect occasionally to flush the records that have
        // been <code>put</code>.

        if (fileWriter == null || fileWriter.getWrittenCount() == 0) {
            // This method is called even if there was no <code>put</code> call.
            return;
        }

        closeLocalChunk();
    }

    public void put(final Collection<SinkRecord> records) throws IOException {
        // <code>SinkTask</code> does not provide a start method, so this method
        // should create the temporary file if not exist, and append the
        // <code>records</code> to the file, and if the temporary file is
        // sufficiently large, then need to flush manually.

        for (SinkRecord rec: records) {
            openLocalChunkIfNotExist(rec);

            fileWriter.write(String.format("%s", String.valueOf(rec.value())));

            // If the size of the file is too large, we need to flush manually.
            if (objectSize > 0 && objectSize >= fileWriter.getSize()) {
                fileWriter.flush();
            } else if (objectCount > 0 && objectCount >= fileWriter.getWrittenCount()) {
                fileWriter.flush();
            }
        }
    }
}
