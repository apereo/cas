package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.kafka.BaseKafkaProperties;
import org.apereo.cas.configuration.model.support.kafka.KafkaSingleTopicProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;

/**
 * This is {@link KafkaEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-events-kafka")
@Getter
@Setter
@Accessors(chain = true)
public class KafkaEventsProperties extends BaseKafkaProperties {
    @Serial
    private static final long serialVersionUID = 6457577229632602109L;


    /**
     * Describe the kafka topic.
     */
    @NestedConfigurationProperty
    private KafkaSingleTopicProperties topic = new KafkaSingleTopicProperties().setName("cas-events");
}
