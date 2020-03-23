package org.apereo.cas.configuration.model.support.services.stream.hazelcast;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.BaseStreamServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link StreamServicesHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-service-registry-stream-hazelcast")
@Getter
@Setter
@Accessors(chain = true)
public class StreamServicesHazelcastProperties extends BaseStreamServicesProperties {

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
    private String duration = "PT1M";

    /**
     * Configuration of the hazelcast instance to queue and stream items.
     */
    @NestedConfigurationProperty
    private BaseHazelcastProperties config = new BaseHazelcastProperties();

    public StreamServicesHazelcastProperties() {
        config.getCluster().setPort(PORT);
        config.getCluster().setInstanceName("localhost-services-replication");
    }
}
