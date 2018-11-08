package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastJCloudsDiscoveryProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.jclouds.JCloudsDiscoveryStrategyFactory;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * This is {@link HazelcastJCloudsDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HazelcastJCloudsDiscoveryStrategy implements HazelcastDiscoveryStrategy {

    @Override
    public DiscoveryStrategyConfig get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig, final Config configuration, final NetworkConfig networkConfig) {
        val jclouds = cluster.getDiscovery().getJclouds();
        val properties = new HashMap<String, Comparable>();
        if (StringUtils.hasText(jclouds.getCredential())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_CREDENTIAL, jclouds.getCredential());
        }
        if (StringUtils.hasText(jclouds.getCredentialPath())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_CREDENTIAL_PATH, jclouds.getCredentialPath());
        }
        if (StringUtils.hasText(jclouds.getEndpoint())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_ENDPOINT, jclouds.getEndpoint());
        }
        if (StringUtils.hasText(jclouds.getGroup())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_GROUP, jclouds.getGroup());
        }
        if (StringUtils.hasText(jclouds.getIdentity())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_IDENTITY, jclouds.getIdentity());
        }
        if (jclouds.getPort() > 0) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_HZ_PORT, jclouds.getPort());
        }
        if (StringUtils.hasText(jclouds.getProvider())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_PROVIDER, jclouds.getProvider());
        }
        if (StringUtils.hasText(jclouds.getRegions())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_REGIONS, jclouds.getRegions());
        }
        if (StringUtils.hasText(jclouds.getRoleName())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_ROLE_NAME, jclouds.getRoleName());
        }
        if (StringUtils.hasText(jclouds.getTagKeys())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_TAG_KEYS, jclouds.getTagKeys());
        }
        if (StringUtils.hasText(jclouds.getTagValues())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_TAG_VALUES, jclouds.getTagValues());
        }
        if (StringUtils.hasText(jclouds.getZones())) {
            properties.put(HazelcastJCloudsDiscoveryProperties.JCLOUDS_DISCOVERY_ZONES, jclouds.getZones());
        }
        return new DiscoveryStrategyConfig(new JCloudsDiscoveryStrategyFactory(), properties);
    }

}
