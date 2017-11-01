package com.joyent.manta.kafka;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.config.SystemSettingsConfigContext;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class MantaSinkTask extends SinkTask {
    private static Logger logger = LoggerFactory.getLogger(MantaSinkTask.class);

    private MantaClient manta;
    private MantaWriter writer;

    public MantaSinkTask() {
    }

    @Override
    public String version() {
        return new MantaSinkConnector().version();
    }

    @Override
    public void stop() {
        logger.info("MantaSinkTask stop");

        // TODO: Close Manta connection?
    }

    @Override
    public void start(final Map<String, String> props) {
        logger.info("MantaSinkTask start:");

        Properties mantaProps = new Properties();
        mantaProps.putAll(props);

        for (Map.Entry<String, String> entry: props.entrySet()) {
            logger.info("  props[{}]={}", entry.getKey(), entry.getValue());
        }
        // TODO: Open Manta connection?
        // TODO: Create Manta directory

        ConfigContext config = new SystemSettingsConfigContext(false, mantaProps);
        try {
            manta = new MantaClient(config);
            writer = new MantaWriter(manta, props);
        } catch (RuntimeException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    @Override
    public void put(final Collection<SinkRecord> records) {
        logger.info("MantaSinkTask put #records={}", records.size());

        try {
            writer.put(records);
        } catch (IOException e) {
            throw new ConnectException("IOException in MantaWriter::put", e);
        }
    }

    @Override
    public void flush(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new ConnectException("IOException in MantaWriter::flush", e);
        }
    }
}
