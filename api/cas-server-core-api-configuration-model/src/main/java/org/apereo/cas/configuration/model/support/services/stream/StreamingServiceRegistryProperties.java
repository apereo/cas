package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.apereo.cas.configuration.support.RequiresModule;

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
public class StreamingServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = 4957127900906059461L;
    /**
     * Indicates the replication mode. Accepted values are:
     *
     * <ul>
     * <li>{@code ACTIVE_ACTIVE}: All CAS nodes sync copies of definitions and keep them locally.</li>
     * <li>{@code ACTIVE_PASSIVE}: One master node keeps definitions and streams changes to other passive nodes</li>
     * </ul>
     */
    private ReplicationModes replicationMode = ReplicationModes.ACTIVE_PASSIVE;
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

    public enum ReplicationModes {

        /**
         * In this replication mode, all CAS nodes will try to sync copies
         * of service definition files individually on each node.
         */
        ACTIVE_ACTIVE,
        /**
         * In this replication mode, one CAS service is designated to be the master
         * that contains all service definition files locally, and will stream changes
         * to other CAS passive nodes. Passive CAS nodes only access the replication
         * cache to retrieve services, and will not individually keep copies of the
         * service definition files on disk.
         */
        ACTIVE_PASSIVE
    }
}
