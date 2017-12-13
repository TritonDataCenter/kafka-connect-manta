package com.joyent.manta.kafka;

import com.joyent.manta.config.ConfigContext;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Properties;

public class MantaSinkConfigDef extends ConfigDef {

    public static final String HTTP_TIMEOUT = "manta.http.timeout";
    public static final String HTTP_RETRIES = "manta.http.retries";
    public static final String MAX_CONNS = "manta.maxconn";
    public static final String KEY_PATH = "manta.key_path";
    public static final String HTTP_BUFFER_SIZE = "manta.http.buffersize";
    public static final String HTTPS_PROTOCOL = "manta.https.protocol";
    public static final String MANTA_OBJECT_PATTERN = "manta.object.pattern";
    public static final String MANTA_OBJECT_PATTERN_DEFAULT = "~~/stor/kafka/%t/%pp/%yyyy-%MM-%dd-%HH-%mm-%ss-%oooooooooooooooooooo.data";
    public static final String MANTA_OBJECT_CLASS = "manta.object.class";
    public static final String MANTA_OBJECT_LIMIT_COUNT = "manta.object.count";
    public static final String MANTA_OBJECT_LIMIT_SIZE = "manta.object.size";
    public static final String MANTA_SIMULATE_FAILURE = "manta.failure";
    public static final String MANTA_URL = "manta.url";

    // TODO: Add more from DefaultsConfigContext

    private ConfigDef config;

    public MantaSinkConfigDef(final ConfigContext context) {} // TODO: import settings from ConfigContext?

    public MantaSinkConfigDef(final Properties props) {
        // super(config, props);

    }

    public MantaSinkConfigDef() {
        super();

        define(KEY_PATH, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Manta Key Location");


        define(MANTA_OBJECT_PATTERN, Type.STRING, MANTA_OBJECT_PATTERN_DEFAULT, Importance.HIGH, "Manta pathname for kafka topic");
        define(MANTA_OBJECT_CLASS, Type.STRING, "java.util.zip.GZIPOutputStream", Importance.HIGH, "Type of stream");
        define(MANTA_URL, Type.STRING, "https://us-east.manta.joyent.com", Importance.HIGH, "Manta service endpoint");

        define(MANTA_OBJECT_LIMIT_COUNT, Type.LONG, -1, Importance.LOW, "Limit number of records per object");
        define(MANTA_OBJECT_LIMIT_SIZE, Type.LONG, -1, Importance.LOW, "Limit the object file size");

        define(MANTA_SIMULATE_FAILURE, Type.STRING, "", Importance.LOW, "Raise an exception if this file exists");

    }

}
