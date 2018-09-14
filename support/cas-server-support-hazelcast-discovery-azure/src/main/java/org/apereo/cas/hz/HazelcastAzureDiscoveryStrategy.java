package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastAzureDiscoveryProperties;

import com.hazelcast.azure.AzureDiscoveryStrategyFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * This is {@link HazelcastAzureDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HazelcastAzureDiscoveryStrategy implements HazelcastDiscoveryStrategy {
    @Override
    public DiscoveryStrategyConfig get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig, final Config configuration, final NetworkConfig networkConfig) {
        val azure = cluster.getDiscovery().getAzure();
        val properties = new HashMap<String, Comparable>();
        if (StringUtils.hasText(azure.getClientId())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_CLIENT_ID, azure.getClientId());
        }
        if (StringUtils.hasText(azure.getClientSecret())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_CLIENT_SECRET, azure.getClientSecret());
        }
        if (StringUtils.hasText(azure.getClusterId())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_CLUSTER_ID, azure.getClusterId());
        }
        if (StringUtils.hasText(azure.getGroupName())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_GROUP_NAME, azure.getGroupName());
        }
        if (StringUtils.hasText(azure.getSubscriptionId())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_SUBSCRIPTION_ID, azure.getSubscriptionId());
        }
        if (StringUtils.hasText(azure.getTenantId())) {
            properties.put(HazelcastAzureDiscoveryProperties.AZURE_DISCOVERY_TENANT_ID, azure.getTenantId());
        }
        return new DiscoveryStrategyConfig(new AzureDiscoveryStrategyFactory(), properties);
    }
}
