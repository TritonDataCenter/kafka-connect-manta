package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * This class will generate Manta pathname based on the provided
 * format specifier.
 *
 * The <code>format</code> may contains one or more escape sequence,
 * which begins with <code>'%'</code> character, and one or more same
 * characters will follow.  The number of following characters will
 * determine the width of the field.
 *
 *
 * <p><code>%%</code> -- literal '%'</p>
 * <p><code>%p</code> -- the Kafka partition id</p>
 * <p><code>%o</code> -- the Kafka offset</p>
 * <p><code>%t</code> -- the Kafka topic</p>
 * <p><code>%y</code> -- year</code></p>
 * <p><code>%D</code> -- day of year</code></p>
 * <p><code>%M</code> -- month of year</code></p>
 * <p><code>%d</code> -- day of month</code></p>
 * <p><code>%H</code> -- hour of day</code></p>
 * <p><code>%m</code> -- minute of hour</code></p>
 * <p><code>%s</code> -- second of minute</code></p>
 *
 * If <code>format</code> starts with '~~/', then it will be
 * substituted to the user's home directory of Manta.
 *
 * For example, the <code>format</code>
 * <code>"~~/stor/kafka/%t/%pp/%yyyy-%MM-%dd-%HH-%mm-%ss-%oooooooooooooooooooo.msg.gz"</code>
 * expands to
 * <code>"/csk/stor/kafka/test/03/2017-10-23-08-29-13-00000000000000024416.msg.gz"</code>.
 */
public class MantaPathname {
    private DateTimeFormatter formatter;
    private String format;
    private SinkRecord record;
    private ConfigContext context;

    /**
     * Create a <code>MantaPathname</code> instance.
     *
     * @param context Manta config context
     * @param format format specifier for the pathname.  See the
     *               documentation of this class for more.
     * @param record the first Kafka <code>SinkRecord</code> for this instance.
     */
    public MantaPathname(final ConfigContext context, final String format,
                         final SinkRecord record) {
        this.format = format;
        this.context = context;
        this.record = record;
    }

    private int eatUp(final String s, final int startPos, final char ch) {
        int ate = 0;
        for (int i = startPos; i < s.length(); i++) {
            if (s.charAt(i) == ch) {
                ate++;
            } else {
                break;
            }
        }
        return ate;
    }

    /**
     * Get the directory part of the Manta pathname
     *
     * @return the directory part of the Manta pathname.
     */
    public String getDirectory() {
        Path p = Paths.get(toString());
        return p.getParent().toString();
    }

    /*
     * By convention, Manta command-line tool uses '~~/' prefix to refer the
     * user's home directory. This function will substitute '~~/' prefix in the
     * MantaPathname to mimic that behavior.
     */
    String toString(final ZonedDateTime dt) {
        if (formatter == null) {
            formatter = createFormatBuilder();
        }
        String p = dt.format(formatter);

        if (p.startsWith("~~/")) {
            return Paths.get(context.getMantaHomeDirectory(), p.substring(3)).toString();
        } else if (p.startsWith("/")) {
            return p;
        } else {
            return Paths.get(context.getMantaHomeDirectory(), p).toString();
        }
    }

    /**
     * Returns the absolute Manta pathname with all parameters rendered.
     *
     * @return teh absolute Manta pathname.
     */
    @Override
    public String toString() {
        return toString(ZonedDateTime.now(ZoneOffset.UTC));
    }

    private DateTimeFormatter createFormatBuilder() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();

        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (ch != '%') {
                builder.appendLiteral(ch);
                continue;
            }

            char fs = format.charAt(i + 1);
            int ate;
            if (fs != '%') {
                ate = eatUp(format, i + 1, fs);
            } else {
                ate = 1;
            }

            switch (fs) {
                case '%':
                    builder.appendLiteral('%');
                    break;
                case 'p':
                    builder.appendLiteral(String.format(String.format("%%0%dd", ate),
                            record.kafkaPartition()));
                    break;
                case 'o':
                    builder.appendLiteral(String.format(String.format("%%0%dd", ate),
                            record.kafkaOffset()));
                    break;
                case 't':
                    builder.appendLiteral(record.topic());
                    break;
                case 'y':
                    builder.appendValue(ChronoField.YEAR, ate);
                    break;
                case 'D':
                    builder.appendValue(ChronoField.DAY_OF_YEAR, ate);
                    break;
                case 'M':
                    builder.appendValue(ChronoField.MONTH_OF_YEAR, ate);
                    break;
                case 'd':
                    builder.appendValue(ChronoField.DAY_OF_MONTH, ate);
                    break;
                case 'H':
                    builder.appendValue(ChronoField.HOUR_OF_DAY, ate);
                    break;
                case 'm':
                    builder.appendValue(ChronoField.MINUTE_OF_HOUR, ate);
                    break;
                case 's':
                    builder.appendValue(ChronoField.SECOND_OF_MINUTE, ate);
                    break;
                default:
                    break;
            }
            i += ate;
        }
        return builder.toFormatter();
    }

}
