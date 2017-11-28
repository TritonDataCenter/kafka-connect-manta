package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class MantaPathname {
    private DateTimeFormatter formatter;
    private String format;
    private SinkRecord record;
    private ConfigContext context;

    public MantaPathname(final ConfigContext context, final String format, final SinkRecord record) {
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

    public String getDirectory() {
        Path p = Paths.get(toString());
        return p.getParent().toString();
    }

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
                    builder.appendLiteral(String.format(String.format("%%0%dd", ate), record.kafkaPartition()));
                    break;
                case 'o':
                    builder.appendLiteral(String.format(String.format("%%0%dd", ate), record.kafkaOffset()));
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

    private static OutputStream createOutputStream(final String className, final String pathname) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor(OutputStream.class);
            OutputStream src = new FileOutputStream(pathname);
            OutputStream os = (OutputStream)ctor.newInstance(src);
            return os;
        } catch (Exception e) {
            throw new RuntimeException("error", e);
        }
    }
}
