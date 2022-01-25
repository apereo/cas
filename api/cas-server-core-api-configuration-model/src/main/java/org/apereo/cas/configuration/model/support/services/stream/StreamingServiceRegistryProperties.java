package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesKafkaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link StreamingServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-service-registry-stream")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("StreamingServiceRegistryProperties")
public class StreamingServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = 4957127900906059461L;

    /**
     * Core stream/replication settings for services.
     */
    @NestedConfigurationProperty
    private StreamingServicesCoreProperties core = new StreamingServicesCoreProperties();

    /**
     * Stream services with hazelcast.
     */
    @NestedConfigurationProperty
    private StreamServicesHazelcastProperties hazelcast = new StreamServicesHazelcastProperties();

    /**
     * Stream services with Kafka.
     */
    @NestedConfigurationProperty
    private StreamServicesKafkaProperties kafka = new StreamServicesKafkaProperties();
}
