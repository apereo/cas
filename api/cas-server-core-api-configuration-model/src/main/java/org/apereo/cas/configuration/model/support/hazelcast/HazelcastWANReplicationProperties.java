package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HazelcastWANReplicationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastWANReplicationProperties implements Serializable {
    private static final long serialVersionUID = 1726420607045775145L;

    /**
     * Whether WAN should be enabled.
     */
    private boolean enabled;

    /**
     * Name of this replication group.
     */
    private String replicationName = "apereo-cas";

    /**
     * List of target clusters to be used for synchronization and replication.
     */
    private List<HazelcastWANReplicationTargetClusterProperties> targets = new ArrayList<>(0);
}
