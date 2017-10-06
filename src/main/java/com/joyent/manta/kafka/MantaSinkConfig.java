package com.joyent.manta.kafka;

import org.apache.kafka.common.config.AbstractConfig;

import java.util.Map;
import java.util.Properties;

public class MantaSinkConfig extends AbstractConfig {
    public static MantaSinkConfigDef configDef = new MantaSinkConfigDef();

    public MantaSinkConfig(Map<String, String> props) {
        super(configDef, props);

    }
}
