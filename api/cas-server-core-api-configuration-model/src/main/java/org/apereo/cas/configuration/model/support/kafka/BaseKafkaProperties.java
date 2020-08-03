package org.apereo.cas.configuration.model.support.kafka;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseKafkaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-kafka-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseKafkaProperties implements Serializable {
    private static final long serialVersionUID = -3844529231331941592L;

    /**
     * Kafka bootstrapping server address (i.e. localhost:9092).
     */
    private String bootstrapAddress;
}
