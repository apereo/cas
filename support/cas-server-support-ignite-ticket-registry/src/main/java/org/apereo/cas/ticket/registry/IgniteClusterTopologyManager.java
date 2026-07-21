package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.ignite.IgniteServer;

/**
 * This is {@link IgniteClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class IgniteClusterTopologyManager implements ClusterTopologyManager {
    private final IgniteServer igniteServerInstance;

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val ignite = igniteServerInstance.api();
        return ignite.cluster()
            .nodes()
            .stream()
            .map(node -> ClusterMember.builder()
                .owner(igniteServerInstance.name())
                .id(node.id().toString())
                .address(node.address().toString())
                .status(true)
                .attributes(Map.of(
                    "restHost", Objects.requireNonNull(node.nodeMetadata()).restHost(),
                    "name", node.name(),
                    "local", node.name().equals(ignite.name()))
                )
                .build())
            .toList();
    }
}
