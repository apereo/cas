package org.apereo.cas.configuration.model.support.services.stream.hazelcast;

import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.BaseStreamServicesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link StreamServicesHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-service-registry-stream-hazelcast")
public class StreamServicesHazelcastProperties extends BaseStreamServicesProperties implements Serializable {
    private static final long serialVersionUID = -1583614089051161614L;

    /**
     * Default port.
     */
    private static final int PORT = 5801;

    /**
     * Duration that indicates how long should items be kept in the hazelcast cache.
     * Note that generally this number needs to be short as once an item is delivered
     * to a target, it is explicitly removed from the cache/queue. This duration needs to be
     * adjusted if the latency between the CAS nodes in the cluster is too large. Having too
     * short a value will cause the record to expire before it reaches other members of the cluster.
     */
    private String duration = "PT30S";

    /**
     * Configuration of the hazelcast instance to queue and stream items.
     */
    @NestedConfigurationProperty
    private BaseHazelcastProperties config = new BaseHazelcastProperties();

    /**
     * Settings that deal with setting up a threaded pool
     * to spawn worker threads from the pool and process events.
     */
    @NestedConfigurationProperty
    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();
    
    public StreamServicesHazelcastProperties() {
        config.getCluster().setPort(PORT);
    }

    public BaseHazelcastProperties getConfig() {
        return config;
    }

    public void setConfig(final BaseHazelcastProperties config) {
        this.config = config;
    }

    public long getDuration() {
        return Beans.newDuration(this.duration).toMillis();
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }

    public ConnectionPoolingProperties getPool() {
        return pool;
    }

    public void setPool(final ConnectionPoolingProperties pool) {
        this.pool = pool;
    }
}
