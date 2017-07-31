package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.model.support.services.stream.amqp.StreamServicesAmqpProperties;
import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link StreamingServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class StreamingServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = 4957127900906059461L;
    @NestedConfigurationProperty
    private StreamServicesHazelcastProperties hazelcast = new StreamServicesHazelcastProperties();
    
    @NestedConfigurationProperty
    private StreamServicesAmqpProperties amqp = new StreamServicesAmqpProperties();

    public StreamServicesHazelcastProperties getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(final StreamServicesHazelcastProperties hazelcast) {
        this.hazelcast = hazelcast;
    }

    public StreamServicesAmqpProperties getAmqp() {
        return amqp;
    }

    public void setAmqp(final StreamServicesAmqpProperties amqp) {
        this.amqp = amqp;
    }
}
