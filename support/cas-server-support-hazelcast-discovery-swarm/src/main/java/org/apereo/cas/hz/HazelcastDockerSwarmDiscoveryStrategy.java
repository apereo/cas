package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastDockerSwarmDiscoveryProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bitsofinfo.hazelcast.discovery.docker.swarm.DockerSwarmDiscoveryStrategyFactory;
import org.bitsofinfo.hazelcast.discovery.docker.swarm.SwarmMemberAddressProvider;
import org.bitsofinfo.hazelcast.spi.docker.swarm.dnsrr.DockerDNSRRMemberAddressProvider;
import org.bitsofinfo.hazelcast.spi.docker.swarm.dnsrr.discovery.DockerDNSRRDiscoveryStrategyFactory;

import java.util.HashMap;
import java.util.Properties;

/**
 * This is {@link HazelcastDockerSwarmDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class HazelcastDockerSwarmDiscoveryStrategy implements HazelcastDiscoveryStrategy {

    @SneakyThrows
    private static DiscoveryStrategyConfig getDiscoveryStrategyConfigViaDnsProvider(final NetworkConfig networkConfig,
                                                                                    final HazelcastDockerSwarmDiscoveryProperties.DnsRProvider dnsProvider) {
        networkConfig.setPortAutoIncrement(false);
        val memberAddressProviderConfig = networkConfig.getMemberAddressProviderConfig();
        memberAddressProviderConfig.setEnabled(true);

        val props = new Properties();
        props.put("serviceName", dnsProvider.getServiceName());
        props.put("servicePort", dnsProvider.getServicePort());
        memberAddressProviderConfig.setImplementation(new DockerDNSRRMemberAddressProvider(props));

        val properties = new HashMap<String, Comparable>();
        if (StringUtils.isNotBlank(dnsProvider.getPeerServices())) {
            properties.put("peerServicesCsv", dnsProvider.getPeerServices());
        }
        return new DiscoveryStrategyConfig(new DockerDNSRRDiscoveryStrategyFactory(), properties);
    }

    private static DiscoveryStrategyConfig getDiscoveryStrategyConfigViaMemberAddressProvider(final Config configuration, final NetworkConfig networkConfig,
                                                                                              final HazelcastDockerSwarmDiscoveryProperties.MemberAddressProvider memberProvider) {
        configuration.setProperty(BaseHazelcastProperties.SHUT_DOWN_HOOK_ENABLED_PROP, Boolean.TRUE.toString());
        configuration.setProperty(BaseHazelcastProperties.SOCKET_BIND_ANY_PROP, Boolean.FALSE.toString());

        val memberAddressProviderConfig = networkConfig.getMemberAddressProviderConfig();
        memberAddressProviderConfig.setEnabled(true);
        memberAddressProviderConfig.setImplementation(new SwarmMemberAddressProvider());

        val properties = new HashMap<String, Comparable>();
        if (StringUtils.isNotBlank(memberProvider.getDockerNetworkNames())) {
            properties.put("docker-network-names", memberProvider.getDockerNetworkNames());
        }
        if (StringUtils.isNotBlank(memberProvider.getDockerNetworkNames())) {
            properties.put("docker-service-names", memberProvider.getDockerServiceNames());
        }
        if (StringUtils.isNotBlank(memberProvider.getDockerServiceLabels())) {
            properties.put("docker-service-labels", memberProvider.getDockerServiceLabels());
        }
        if (StringUtils.isNotBlank(memberProvider.getSwarmMgrUri())) {
            properties.put("swarm-mgr-uri", memberProvider.getSwarmMgrUri());
            properties.put("skip-verify-ssl", memberProvider.isSkipVerifySsl());
        }
        if (memberProvider.getHazelcastPeerPort() > 0) {
            properties.put("hazelcast-peer-port", memberProvider.getHazelcastPeerPort());
        }
        return new DiscoveryStrategyConfig(new DockerSwarmDiscoveryStrategyFactory(), properties);
    }

    @Override
    public DiscoveryStrategyConfig get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig, final Config configuration, final NetworkConfig networkConfig) {
        val dockerSwarm = cluster.getDiscovery().getDockerSwarm();
        val memberProvider = dockerSwarm.getMemberProvider();
        val dnsProvider = dockerSwarm.getDnsProvider();

        if (memberProvider.isEnabled()) {
            return getDiscoveryStrategyConfigViaMemberAddressProvider(configuration, networkConfig, memberProvider);
        }
        if (dnsProvider.isEnabled()) {
            return getDiscoveryStrategyConfigViaDnsProvider(networkConfig, dnsProvider);
        }
        throw new IllegalArgumentException("No discovery strategy is turned on and enabled in configuration");
    }

}
