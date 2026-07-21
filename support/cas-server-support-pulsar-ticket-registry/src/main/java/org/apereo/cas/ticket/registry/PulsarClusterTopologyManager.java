package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.pulsar.core.PulsarAdministration;

/**
 * This is {@link PulsarClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class PulsarClusterTopologyManager implements ClusterTopologyManager {
    private final PulsarAdministration pulsarAdministration;

    @Override
    public List<? extends ClusterMember> discoverMembers() throws Exception {
        try (val pulsarAdmin = pulsarAdministration.createAdminClient()) {
            val leaderBroker = pulsarAdmin.brokers().getLeaderBroker();
            val leaderServiceUrl = leaderBroker != null ? leaderBroker.getServiceUrl() : null;
            return pulsarAdmin
                .clusters()
                .getClusters()
                .stream()
                .flatMap(Unchecked.function(clusterName -> {
                    val brokers = pulsarAdmin.brokers().getActiveBrokers(clusterName);
                    return brokers
                        .stream()
                        .map(broker -> toClusterMember(clusterName, broker, leaderServiceUrl));
                }))
                .toList();
        }
    }

    private ClusterMember toClusterMember(final String clusterName, final String broker,
                                          @Nullable final String leaderServiceUrl) {
        val address = parseAddress(broker);
        val leader = isLeaderBroker(broker, leaderServiceUrl);
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("cluster", clusterName);
        attributes.put("host", address.host());
        attributes.put("port", address.port());
        attributes.put("role", leader ? "LEADER_BROKER" : "BROKER");
        attributes.put("state", "ACTIVE");
        return ClusterMember.builder()
            .owner(getName())
            .id(broker)
            .address("%s:%d".formatted(address.host(), address.port()))
            .status(true)
            .attributes(attributes)
            .build();
    }

    private static boolean isLeaderBroker(final String broker, @Nullable final String leaderServiceUrl) {
        if (leaderServiceUrl == null) {
            return false;
        }
        val leader = parseAddress(leaderServiceUrl);
        val member = parseAddress(broker);
        return leader.host().equalsIgnoreCase(member.host()) && leader.port() == member.port();
    }

    private static BrokerAddress parseAddress(final String value) {
        try {
            val uri = value.contains("://")
                ? URI.create(value)
                : URI.create("http://" + value);
            return new BrokerAddress(uri.getHost(), uri.getPort());
        } catch (final Exception e) {
            return new BrokerAddress(value, -1);
        }
    }

    private record BrokerAddress(String host, int port) {
    }
}
