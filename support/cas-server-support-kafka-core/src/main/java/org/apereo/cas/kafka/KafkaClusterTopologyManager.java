package org.apereo.cas.kafka;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.Node;

/**
 * This is {@link KafkaClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class KafkaClusterTopologyManager implements ClusterTopologyManager {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final AdminClient kafkaAdminClient;

    @Override
    public List<? extends ClusterMember> discoverMembers() throws Exception {
        val result = kafkaAdminClient.describeCluster();
        val nodes = result.nodes().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        val controller = result.controller().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        val clusterId = result.clusterId().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        return nodes
            .stream()
            .map(node -> toClusterMember(node, controller, clusterId))
            .toList();
    }

    private ClusterMember toClusterMember(final Node node, final Node controller, final String clusterId) {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("clusterId", clusterId);
        attributes.put("brokerId", node.id());
        attributes.put("host", node.host());
        attributes.put("port", node.port());
        attributes.put("fenced", node.isFenced());
        attributes.put("rack", node.rack() != null ? node.rack() : "UNKNOWN");
        attributes.put("role", node.equals(controller) ? "CONTROLLER" : "BROKER");

        return ClusterMember.builder()
            .owner(getName())
            .id(String.valueOf(node.id()))
            .address("%s:%d".formatted(node.host(), node.port()))
            .status(true)
            .attributes(attributes)
            .build();
    }
}
