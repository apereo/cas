package org.apereo.cas.configuration.model.support.services.stream.hazelcast;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.BaseStreamServicesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link StreamServicesHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class StreamServicesHazelcastProperties extends BaseStreamServicesProperties implements Serializable {
    private static final long serialVersionUID = -1583614089051161614L;

    private static final int PORT = 5801;

    /**
     * Duration that indicates how long should items be kept in the hazelcast cache.
     * Note that generally this number needs to be short as once an item is delivered
     * to a target, it is explicitly removed from the cache/queue.
     */
    private String duration = "PT10S";
    
    @NestedConfigurationProperty
    private HazelcastProperties config = new HazelcastProperties();

    public StreamServicesHazelcastProperties() {
        config.getCluster().setPort(PORT);
    }

    public HazelcastProperties getConfig() {
        return config;
    }

    public void setConfig(final HazelcastProperties config) {
        this.config = config;
    }

    public long getDuration() {
        return Beans.newDuration(this.duration).toMillis();
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }
}
