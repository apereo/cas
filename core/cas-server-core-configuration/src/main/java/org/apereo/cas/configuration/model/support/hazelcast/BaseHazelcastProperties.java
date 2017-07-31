package org.apereo.cas.configuration.model.support.hazelcast;

import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link BaseHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class BaseHazelcastProperties implements Serializable {
    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";

    /**
     * Max num of seconds for heartbeat property name.
     */
    public static final String MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds";
    /**
     * Ipv4 protocol stack.
     */
    public static final String IPV4_STACK_PROP = "hazelcast.prefer.ipv4.stack";
    private static final long serialVersionUID = 4204884717547468480L;

    /**
     * Location of a {@code hazelcast.xml} file that fully takes over the construction and configuration
     * of hazelcast caches, etc.
     */
    private Resource configLocation;

    /**
     * Hazelcast cluster settings if CAS is able to auto-create caches.
     */
    private HazelcastClusterProperties cluster = new HazelcastClusterProperties();
    
    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public HazelcastClusterProperties getCluster() {
        return cluster;
    }

    public void setCluster(final HazelcastClusterProperties cluster) {
        this.cluster = cluster;
    }
}
