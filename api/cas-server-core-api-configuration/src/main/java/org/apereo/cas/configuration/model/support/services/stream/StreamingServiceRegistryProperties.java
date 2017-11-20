package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link StreamingServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-service-registry-stream")
public class StreamingServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = 4957127900906059461L;

    /**
     * Whether service registry events should be streamed and published
     * across a CAS cluster. One typical workflow is to enable the
     * publisher on one master node and simply have others consume definitions
     * and changes from the upstream master node in order to avoid overrides
     * and timing issues as changes may step over each other if
     * the service registry schedule is not timed correctly.
     */
    private boolean enabled = true;
    
    /**
     * Stream services with hazelcast.
     */
    @NestedConfigurationProperty
    private StreamServicesHazelcastProperties hazelcast = new StreamServicesHazelcastProperties();
    
    public StreamServicesHazelcastProperties getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(final StreamServicesHazelcastProperties hazelcast) {
        this.hazelcast = hazelcast;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
