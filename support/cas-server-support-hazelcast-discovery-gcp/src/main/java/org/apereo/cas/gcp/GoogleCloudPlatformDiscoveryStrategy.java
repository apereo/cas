package org.apereo.cas.gcp;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.hz.HazelcastDiscoveryStrategy;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.gcp.GcpDiscoveryStrategyFactory;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link GoogleCloudPlatformDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class GoogleCloudPlatformDiscoveryStrategy implements HazelcastDiscoveryStrategy {

    @Override
    public Optional<DiscoveryStrategyConfig> get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig,
                                                 final Config configuration, final NetworkConfig networkConfig) {
        val gcp = cluster.getDiscovery().getGcp();
        val properties = new HashMap<String, Comparable>();
        if (StringUtils.isNotBlank(gcp.getPrivateKeyPath())) {
            properties.put("private-key-path", gcp.getPrivateKeyPath());
        }
        if (StringUtils.isNotBlank(gcp.getProjects())) {
            properties.put("projects", gcp.getProjects());
        }
        if (StringUtils.isNotBlank(gcp.getZones())) {
            properties.put("zones", gcp.getZones());
        }
        if (StringUtils.isNotBlank(gcp.getLabel())) {
            properties.put("label", gcp.getLabel());
        }
        if (StringUtils.isNotBlank(gcp.getRegion())) {
            properties.put("region", gcp.getRegion());
        }
        if (StringUtils.isNotBlank(gcp.getHzPort())) {
            properties.put("hz-port", gcp.getHzPort());
        }
        return Optional.of(new DiscoveryStrategyConfig(new GcpDiscoveryStrategyFactory(), properties));
    }

}
