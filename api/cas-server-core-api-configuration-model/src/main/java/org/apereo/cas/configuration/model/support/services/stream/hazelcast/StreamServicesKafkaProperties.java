package org.apereo.cas.configuration.model.support.services.stream.hazelcast;

import org.apereo.cas.configuration.model.support.kafka.BaseKafkaProperties;
import org.apereo.cas.configuration.model.support.kafka.KafkaSingleTopicProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.UUID;

/**
 * This is {@link StreamServicesKafkaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-service-registry-stream-kafka")
@Getter
@Setter
@Accessors(chain = true)
public class StreamServicesKafkaProperties extends BaseKafkaProperties {
    private static final long serialVersionUID = -7126701588226903867L;

    /**
     * Describe the kafka topic.
     */
    @NestedConfigurationProperty
    private KafkaSingleTopicProperties topic = new KafkaSingleTopicProperties();

    public StreamServicesKafkaProperties() {
        topic.setName(UUID.randomUUID().toString());
    }
}
