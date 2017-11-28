package com.joyent.manta.kafka;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObjectResponse;
import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MantaWriterTest {
    private static final String MANTA_OBJECT_DIRECTORY = "Manta Obj Directory";
    private static final String MANTA_OBJECT_PATHNAME = "Manta Obj Pathname";

    private static final String MANTA_RESP_STRING = "MANTA Response String";
    private static final String KAFKA_TOPIC_STRING = "TEST TOPIC";
    private static final int KAFKA_PARTITION = 1;

    @Mock
    private MantaClient manta;

    @Mock
    private MantaObjectResponse response;

    @Mock
    private ConfigContext mantaContext;

    @Mock
    private LocalObjectWriter localObjectWriter;

    @Mock
    private MantaPathname pathname;

    @Mock
    private BufferedInputStream inputStream;

    private Collection<SinkRecord> manyRecords;
    private Collection<SinkRecord> oneRecord;
    private Collection<SinkRecord> zeroRecord;

    @Mock
    private ObjectFactory factory;

    private MantaWriter mantaWriter;
    private Map<String, String> context;

    private File localObjectFile;

    @Before
    public void setUp() throws IOException {
        context = new HashMap<>();
        context.put(MantaSinkConfigDef.MANTA_OBJECT_CLASS, "java.io.BufferedOutputStream");
        context.put(MantaSinkConfigDef.MANTA_OBJECT_PATTERN, "stor/MANTA_FILE_NAME");
        context.put(MantaSinkConfigDef.MANTA_SIMULATE_FAILURE, "false");

        manyRecords = new ArrayList<>();
        for (int i = 0; i < 1234; i++) {
            String value = String.format("message#%d", i);
            manyRecords.add(new SinkRecord(KAFKA_TOPIC_STRING, KAFKA_PARTITION,
                    null, null, null, value, i));
        }

        oneRecord = new ArrayList<>();
        oneRecord.add(new SinkRecord(KAFKA_TOPIC_STRING, KAFKA_PARTITION,
                null, null, null,
                String.format("message#1"), 0));

        zeroRecord = new ArrayList<>();

        mantaWriter = new MantaWriter(manta, context, factory);

        when(factory.getObject(eq(MantaPathname.class),
                any(MantaPathname.class))).thenReturn(pathname);
        when(factory.getObject(eq(LocalObjectWriter.class),
                any(LocalObjectWriter.class))).thenReturn(localObjectWriter);
        when(factory.getObject(eq(BufferedInputStream.class),
                any(BufferedInputStream.class))).thenReturn(inputStream);

        doNothing().when(manta).putDirectory(anyString(), Matchers.anyBoolean());
        when(manta.put(anyString(), any(InputStream.class), anyLong(), any(),
                any())).thenReturn(response);
        when(manta.getContext()).thenReturn(mantaContext);
        when(mantaContext.getMantaHomeDirectory()).thenReturn("/userhome");

        when(response.toString()).thenReturn(MANTA_RESP_STRING);

        localObjectFile = Files.newTemporaryFile();
        when(localObjectWriter.getPath()).thenReturn(localObjectFile.getPath());

        when(pathname.getDirectory()).thenReturn(MANTA_OBJECT_DIRECTORY);
        when(pathname.toString()).thenReturn(MANTA_OBJECT_PATHNAME);
    }

    @After
    public void cleanUp() {
        localObjectFile.delete();
    }

    @Test
    public void flush_withZeroRecord_Ensure_NoMantaOperation() throws IOException {
        mantaWriter.flush();
        mantaWriter.put(zeroRecord);
        mantaWriter.flush();
        mantaWriter.put(zeroRecord);
        mantaWriter.put(zeroRecord);

        when(localObjectWriter.getWrittenCount()).thenReturn(
                Long.valueOf(zeroRecord.size() + zeroRecord.size() + zeroRecord.size()));

        mantaWriter.flush();

        verify(manta, times(0)).put(anyString(),
                any(InputStream.class), anyLong(), any(), any());
    }

    @Test
    public void flush_WithMultipleRecords_Ensure_MantaPutOperation() throws IOException {
        mantaWriter.put(manyRecords);
        mantaWriter.put(oneRecord);

        when(localObjectWriter.getWrittenCount()).thenReturn(
                Long.valueOf(manyRecords.size() + oneRecord.size()));
        mantaWriter.flush();

        verify(manta, times(1)).put(anyString(),
                any(InputStream.class), anyLong(), any(), any());
    }

    @Test
    public void ensure_closeWriterCalled_On_MantaPut_Exception() throws IOException {
        when(manta.put(anyString(), any(InputStream.class), anyLong(), any(), any())).thenThrow(IOException.class);

        try {
            mantaWriter.put(manyRecords);
            mantaWriter.put(oneRecord);
            when(localObjectWriter.getWrittenCount()).thenReturn(
                    Long.valueOf(manyRecords.size() + oneRecord.size()));

            mantaWriter.flush();
        }
        catch (IOException e) {
            ;
        }

        verify(inputStream, atLeast(1)).close();
        verify(localObjectWriter, atLeast(1)).close();
        verify(localObjectWriter, atLeast(1)).delete();
    }

    @Test
    public void ensure_closeWriterCalled_On_MantaPutDirectory_Exception() throws IOException {
        doThrow(IOException.class).when(manta).putDirectory(anyString(), Matchers.anyBoolean());

        try {
            mantaWriter.put(manyRecords);
            mantaWriter.put(oneRecord);
            when(localObjectWriter.getWrittenCount()).thenReturn(
                    Long.valueOf(manyRecords.size() + oneRecord.size()));

            mantaWriter.flush();
        }
        catch (IOException e) {
            ;
        }

        verify(localObjectWriter, atLeast(1)).close();
        verify(localObjectWriter, atLeast(1)).delete();
    }
}
