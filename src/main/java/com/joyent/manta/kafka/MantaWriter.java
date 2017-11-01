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

public class MantaWriter {
    private static Logger log = LoggerFactory.getLogger(MantaWriter.class);

    // out, outPath, and mantaPath will be determined on the first call to put()
    private ObjectFactory factory;
    private LocalObjectWriter fileWriter; // TODO: we may need a Map[Topic, LocalObjectWriter].
    private MantaPathname mantaPathname;

    private String objectPattern;
    private String objectClass;
    private MantaClient manta;
    private SinkRecord firstRecord;
    private long objectCount;
    private long objectSize;
    private String mantaShouldFail;

    public MantaWriter(final MantaClient mantaClient, final Map<String, String> context) {
        this(mantaClient, context, new ObjectFactory());
    }

    public MantaWriter(final MantaClient mantaClient, final Map<String, String> context, final ObjectFactory factory) {
        this.factory = factory;

        this.manta = mantaClient;
        this.objectPattern = context.get(MantaSinkConfigDef.MANTA_OBJECT_PATTERN);
        this.objectClass = context.get(MantaSinkConfigDef.MANTA_OBJECT_CLASS);

        this.objectCount = Long.parseLong(context.getOrDefault(MantaSinkConfigDef.MANTA_OBJECT_LIMIT_COUNT, "-1"));
        this.objectSize = Long.parseLong(context.getOrDefault(MantaSinkConfigDef.MANTA_OBJECT_LIMIT_SIZE, "-1"));
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
            log.warn("closing fileWriter failed", e);
        } finally {
            fileWriter = null;
        }
    }

    void openLocalChunkIfNotExist(final SinkRecord firstRecord) throws IOException {
        if (this.firstRecord != null) {
            return;
        }

        try {
            this.firstRecord = firstRecord;
            fileWriter = factory.getObject(LocalObjectWriter.class, new LocalObjectWriter(objectClass));
            mantaPathname = factory.getObject(MantaPathname.class,
                                              new MantaPathname(manta.getContext(), objectPattern, firstRecord));

            log.info("TEMP[#%d]: {}", firstRecord.kafkaPartition(), fileWriter.getPath());
        } catch (Exception e) {
            this.firstRecord = null;

            closeWriter();
            throw e;
        }
    }

    void closeLocalChunk() throws IOException {
        try {
            fileWriter.close();

            manta.putDirectory(mantaPathname.getDirectory(), true);

            try (InputStream is = factory.getObject(BufferedInputStream.class,
                                                    new BufferedInputStream(new FileInputStream(fileWriter.getPath())))) {
                // MantaObjectResponse resp = manta.put(mantaPathname.toString(), fileWriter.getPath());
                if (!mantaShouldFail.isEmpty() && Files.exists(Paths.get(mantaShouldFail))) {
                    throw new IOException("Simulating Manta client exception.");
                }

                MantaObjectResponse resp = manta.put(mantaPathname.toString(), is, fileWriter.getSize(), null, null);
                log.info("manta resp: {}", resp);
            }
        } catch (IOException e) {
            // ignored
            log.error(String.format("IOException on MantaClient: %s", e.getMessage()), e);
            throw e;
        } catch (Exception e) {
            log.error("uncatched exception", e);
            throw e;
        } finally {
            firstRecord = null;
            closeWriter();
        }
    }

    public void flush() throws IOException {
        if (fileWriter == null || fileWriter.getWrittenCount() == 0) {
            return;
        }

        closeLocalChunk();
    }

    public void put(final Collection<SinkRecord> records) throws IOException {
        for (SinkRecord rec: records) {
            openLocalChunkIfNotExist(rec);

            fileWriter.write(String.format("%s[%d:%10d]: %s",
                                           rec.topic(), rec.kafkaPartition(), rec.kafkaOffset(), String.valueOf(rec.value())));

            // TODO: if the size of the out is too much, we need to flush manually.
            if (objectSize > 0 && objectSize >= fileWriter.getSize()) {
                fileWriter.flush();
            } else if (objectCount > 0 && objectCount >= fileWriter.getWrittenCount()) {
                fileWriter.flush();
            }
        }
    }

}
