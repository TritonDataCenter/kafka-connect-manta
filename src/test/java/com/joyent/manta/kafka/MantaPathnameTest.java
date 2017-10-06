package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;
import org.assertj.core.api.Condition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MantaPathnameTest {

    private MantaPathname mantaPathname;

    @Mock
    private ConfigContext context;

    @Mock
    private SinkRecord sinkRecord;

    private static String MANTA_HOME_DIRECTORY = "/USER-HOME";

    private static int KAFKA_PARTITION = 1234;
    private static long KAFKA_OFFSET = 4321;
    private static String KAFKA_TOPIC = "TOPIC";

    @Before
    public void setUp() {
        when(context.getMantaHomeDirectory()).thenReturn(MANTA_HOME_DIRECTORY);
        when(sinkRecord.kafkaPartition()).thenReturn(KAFKA_PARTITION);
        when(sinkRecord.kafkaOffset()).thenReturn(KAFKA_OFFSET);
        when(sinkRecord.topic()).thenReturn(KAFKA_TOPIC);

    }

    @Test
    public void getDirectory() throws Exception {
        MantaPathname path = new MantaPathname(context, "/foo/bar", sinkRecord);
        assertThat(path.getDirectory()).isEqualTo("/foo");
    }

    @Test
    public void ensure_Generate_UserHome_when_relative_path_used() {
        MantaPathname path = new MantaPathname(context, "stor/kafka/path", sinkRecord);

        assertThat(path.toString()).startsWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void ensure_Generate_UserHome_when_tilde_prefix_used() {
        MantaPathname path = new MantaPathname(context, "~~/stor/kafka/path", sinkRecord);

        assertThat(path.toString()).startsWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void ensure_notGenerating_UserHome_when_absolutePathUsed() {
        MantaPathname path = new MantaPathname(context, "/admin/kafka/path", sinkRecord);

        assertThat(path.toString()).doesNotStartWith(MANTA_HOME_DIRECTORY);
    }

    @Test
    public void verify_format_escaping_escapingChar() {
        MantaPathname path = new MantaPathname(context, "~/%%%%%%-POSTFIX", sinkRecord);

        assertThat(path.toString()).endsWith("/%%%-POSTFIX");
    }

    @Test
    public void verify_format_dateTime_year() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%yy", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yy");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }

    @Test
    public void verify_format_dateTime_monthOfYear() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%MM", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }

    @Test
    public void verify_format_dateTime_dayOfMonth() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%ddd", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("ddd");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }

    @Test
    public void verify_format_dateTime_hourOfDay() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%HH", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }

    @Test
    public void verify_format_dateTime_minuteOfHour() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%mm", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("mm");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }

    @Test
    public void verify_format_dateTime_secondOfMinute() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        MantaPathname path = new MantaPathname(context, "~~/%ss", sinkRecord);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("ss");
        assertThat(path.toString(now)).endsWith(now.toString(formatter));
    }
}
