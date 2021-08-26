package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastDiscoveryProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link HazelcastClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastClusterProperties")
public class HazelcastClusterProperties implements Serializable {

    private static final long serialVersionUID = 1817784607045775145L;

    /**
     * Describe discovery strategies for Hazelcast.
     */
    @NestedConfigurationProperty
    private HazelcastDiscoveryProperties discovery = new HazelcastDiscoveryProperties();

    /**
     * WAN replication settings.
     */
    @NestedConfigurationProperty
    private HazelcastWANReplicationProperties wanReplication = new HazelcastWANReplicationProperties();

    /**
     * Hazelcast core cluster settings.
     */
    @NestedConfigurationProperty
    private HazelcastCoreClusterProperties core = new HazelcastCoreClusterProperties();

    /**
     * Hazelcast network cluster settings.
     */
    @NestedConfigurationProperty
    private HazelcastNetworkClusterProperties network = new HazelcastNetworkClusterProperties();

}
