package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.connect.sink.SinkRecord;

import java.io.IOException;
import java.io.InputStream;

class ObjectFactory {
    <T> T getObject(Class<?> klass, T obj) {
        return obj;
    }
}
