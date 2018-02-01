package org.apereo.cas.hz;

import com.hazelcast.aws.AwsDiscoveryStrategyFactory;
import com.hazelcast.config.DiscoveryStrategyConfig;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastAwsDiscoveryProperties;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link HazelcastAwsDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class HazelcastAwsDiscoveryStrategy implements HazelcastDiscoveryStrategy {

    @Override
    public DiscoveryStrategyConfig get(final HazelcastClusterProperties cluster) {
        final HazelcastAwsDiscoveryProperties aws = cluster.getDiscovery().getAws();
        final Map<String, Comparable> properties = new HashMap<>();
        if (StringUtils.hasText(aws.getAccessKey())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_ACCESS_KEY, aws.getAccessKey());
        }
        if (StringUtils.hasText(aws.getSecretKey())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_SECRET_KEY, aws.getSecretKey());
        }
        if (StringUtils.hasText(aws.getIamRole())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_IAM_ROLE, aws.getIamRole());
        }
        if (StringUtils.hasText(aws.getHostHeader())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_HOST_HEADER, aws.getHostHeader());
        }
        if (aws.getPort() > 0) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_PORT, aws.getPort());
        }
        if (StringUtils.hasText(aws.getRegion())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_REGION, aws.getRegion());
        }
        if (StringUtils.hasText(aws.getSecurityGroupName())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_SECURITY_GROUP_NAME, aws.getSecurityGroupName());
        }
        if (StringUtils.hasText(aws.getTagKey())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_TAG_KEY, aws.getTagKey());
        }
        if (StringUtils.hasText(aws.getTagValue())) {
            properties.put(HazelcastAwsDiscoveryProperties.AWS_DISCOVERY_TAG_VALUE, aws.getTagValue());
        }

        return new DiscoveryStrategyConfig(new AwsDiscoveryStrategyFactory(), properties);
    }

}
