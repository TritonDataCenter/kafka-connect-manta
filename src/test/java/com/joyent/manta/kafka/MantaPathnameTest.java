package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MantaPathnameTest {
    private static final String MANTA_HOME_DIRECTORY = "/USER-HOME";
    private static final ZonedDateTime TEST_TIME =
            ZonedDateTime.parse("2017-11-08T21:05:07.793Z");
    private static final int KAFKA_PARTITION = 1234;
    private static final long KAFKA_OFFSET = 4321;
    private static final String KAFKA_TOPIC = "TOPIC";

    @Mock
    private ConfigContext context;

    @Mock
    private SinkRecord sinkRecord;

    @Before
    public void setUp() {
        when(context.getMantaHomeDirectory()).thenReturn(MANTA_HOME_DIRECTORY);
        when(sinkRecord.kafkaPartition()).thenReturn(KAFKA_PARTITION);
        when(sinkRecord.kafkaOffset()).thenReturn(KAFKA_OFFSET);
        when(sinkRecord.topic()).thenReturn(KAFKA_TOPIC);

    }

    @Test
    public void getDirectory() throws Exception {
        MantaPathname path = new MantaPathname(context, "/foo/bar",
                sinkRecord);
        assertThat(path.getDirectory()).isEqualTo("/foo");
    }

    @Test
    public void ensure_Generate_UserHome_when_relative_path_used() {
        MantaPathname path = new MantaPathname(context, "stor/kafka/path",
                sinkRecord);

        assertThat(path.toString()).startsWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void ensure_Generate_UserHome_when_tilde_prefix_used() {
        MantaPathname path = new MantaPathname(context, "~~/stor/kafka/path",
                sinkRecord);

        assertThat(path.toString()).startsWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void ensure_notGenerating_UserHome_when_absolutePathUsed() {
        MantaPathname path = new MantaPathname(context, "/admin/kafka/path",
                sinkRecord);

        assertThat(path.toString()).doesNotStartWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void verify_format_escaping_escapingChar() {
        assertPathLastSegmentEquals("~/%%%%%%-POSTFIX", "%%%-POSTFIX");
    }

    @Test
    public void verify_format_dateTime_four_digit_year() {
        assertPathLastSegmentEquals("~~/%yyyy", "2017");
    }

    @Test(expected = DateTimeException.class)
    public void verify_format_dateTime_two_digit_year_doesnt_work() {
        assertPathLastSegmentEquals("~~/%yy", "17");
    }

    @Test
    public void verify_format_dateTime_monthOfYear() {
        assertPathLastSegmentEquals("~~/%MM", "11");
    }

    @Test
    public void verify_format_dateTime_dayOfMonth() {
        assertPathLastSegmentEquals("~~/%dd", "08");
    }

    @Test
    public void verify_format_dateTime_hourOfDay() {
        assertPathLastSegmentEquals("~~/%HH", "21");
    }

    @Test
    public void verify_format_dateTime_minuteOfHour() {
        assertPathLastSegmentEquals("~~/%mm", "05");
    }

    @Test
    public void verify_format_dateTime_secondOfMinute() {
        assertPathLastSegmentEquals("~~/%ss", "07");
    }

    private void assertPathLastSegmentEquals(final String pathLiteral,
                                             final String expected) {
        MantaPathname pathname = new MantaPathname(context, pathLiteral, sinkRecord);
        String mantaPath = pathname.toString(TEST_TIME);
        String lastSegment = Paths.get(mantaPath).getFileName().toString();

        assertEquals("Last segment in Manta path [" + mantaPath + "] "
                + "did not equal the expected value",
                expected, lastSegment);
    }
}
