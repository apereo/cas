package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link StreamingServicesCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-service-registry-stream")
@Accessors(chain = true)
public class StreamingServicesCoreProperties implements Serializable {

    private static final long serialVersionUID = 2957227900906059461L;

    /**
     * Indicates the replication mod.
     */
    private ReplicationModes replicationMode = ReplicationModes.PASSIVE;

    /**
     * Whether service registry events should be streamed and published
     * across a CAS cluster. One typical workflow is to enable the
     * publisher on one master node and have others consume definitions
     * and changes from the upstream master node in order to avoid overrides
     * and timing issues as changes may step over each other if
     * the service registry schedule is not timed correctly.
     */
    private boolean enabled = true;

    public enum ReplicationModes {

        /**
         * In this replication mode, all CAS nodes will try to sync copies
         * of service definition files individually on each node.
         */
        ACTIVE,
        /**
         * In this replication mode, one CAS service is designated to be the master
         * that contains all service definition files locally, and will stream changes
         * to other CAS passive nodes. Passive CAS nodes only access the replication
         * cache to retrieve services, and will not individually keep copies of the
         * service definition files on disk.
         */
        PASSIVE
    }
}
