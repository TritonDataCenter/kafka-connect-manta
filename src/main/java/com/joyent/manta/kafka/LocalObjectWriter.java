package com.joyent.manta.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class LocalObjectWriter implements AutoCloseable, Closeable {
    private static Logger log = LoggerFactory.getLogger(LocalObjectWriter.class);

    private File file;
    private long writtenBytes;
    private long writtenCount;
    private PrintWriter writer;

    public LocalObjectWriter(final String className) throws IOException {
        file = File.createTempFile("kafka-manta-sink", null);
        writer = new PrintWriter(new OutputStreamWriter(createStream(className, file), StandardCharsets.UTF_8), false);
    }

    private OutputStream createStream(final String className, final File newFile) throws FileNotFoundException {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor(OutputStream.class);
            OutputStream src = new FileOutputStream(newFile);
            return (OutputStream) ctor.newInstance(src);
        } catch (Exception e) {
            log.warn(String.format("Creating instance of %s failed, using BufferredOutputStream.", className), e);
            return new BufferedOutputStream(new FileOutputStream(newFile));
        }
    }

    public void write(final Object o) {
        String s = String.valueOf(o);
        writer.println(o);
        writtenBytes += s.length();
        writtenCount++;
    }

    public void flush() {
        writer.flush();
    }

    public long getWrittenBytes() {
        return writtenBytes;
    }

    public long getWrittenCount() {
        return writtenCount;
    }

    public long getSize() {
        writer.flush();
        return file.length();
    }

    public String getPath() {
        return file.getPath();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    public void delete() {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            // ignored
        }
    }
}
