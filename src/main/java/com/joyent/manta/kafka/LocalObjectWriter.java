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

/**
 * This class represent a file writer, which appends objects into a
 * temporary file, until it is closed.
 *
 * <p>Users can choose the internal stream representation by providing
 * the stream class name in the constructor.</p>
 */
public class LocalObjectWriter implements AutoCloseable, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(LocalObjectWriter.class);

    private File file;
    private long writtenBytes;
    private long writtenCount;
    private PrintWriter writer;

    /**
     * Create a <code>LocalObjectWriter</code>.
     *
     * <code>className</code> should be a fully qualified class name
     * and it must have a constructor that receives
     * <code>OutputStream</code> as an input, and must return an
     * <code>OutputStream</code>.  For example you could provide
     * <code>"java.io.BufferedOutputStream"</code> or
     * <code>"java.util.zip.GZIPOutputstream"</code>.
     *
     * @param className Stream class name that is used for storing objects.
     *
     * @throws IOException
     */
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
            LOG.warn(String.format("Creating instance of %s failed, using BufferredOutputStream.", className), e);
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

    /**
     * Return the current size of the underlying file.
     *
     * @return the byte size of the underlying file.
     */
    public long getSize() {
        // Note that this may not be the ideal way to get the actual size of the file, However, if the user gave
        // java.util.zip.GZIPOutputstream as the type of the stream in the constructor, there is no way to get the
        // actual size of the file from the stream itself.
        writer.flush();
        return file.length();
    }

    /**
     * Return the full pathname of the underlying file.
     *
     * @return pathname of the underlying file.
     */
    public String getPath() {
        return file.getPath();
    }

    /**
     * Close the underlying file
     *
     * Note that this method will not delete the underlying file.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    /**
     * Delete the underlying file.  On Unix-like system you may able to call
     * this method before closing the file.
     */
    public void delete() {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            // ignored
        }
    }
}
