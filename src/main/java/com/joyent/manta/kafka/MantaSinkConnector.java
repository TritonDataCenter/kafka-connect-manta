package com.joyent.manta.kafka;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MantaSinkConnector extends SinkConnector {
    private static final Logger LOG = LoggerFactory.getLogger(MantaSinkConnector.class);

    private static final String VERSION = "alpha";

    private MantaSinkConfig sinkConfig;

    public MantaSinkConnector() {
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return MantaSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(final int maxTasks) {
        LOG.info("MantaSinkConnector taskConfigs({})", maxTasks);

        ArrayList<Map<String, String>> l = new ArrayList<>();

        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<String, String>(
                    sinkConfig.originalsStrings());
            l.add(config);
        }

        return l;
    }

    @Override
    public void stop() {
        LOG.info("MantaSinkConnector stop");
    }

    @Override
    public void start(final Map<String, String> props) {
        LOG.info("MantaSinkConnector start");

        try {
            sinkConfig = new MantaSinkConfig(props);
        } catch (Exception e) {
            throw new ConnectException("configuration error", e);
        }
    }

    @Override
    public ConfigDef config() {
        return MantaSinkConfig.configDef;
    }

}
