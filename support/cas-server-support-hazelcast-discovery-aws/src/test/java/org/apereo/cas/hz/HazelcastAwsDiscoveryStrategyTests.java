package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.configuration.model.support.hazelcast.discovery.HazelcastAwsDiscoveryProperties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link HazelcastAwsDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HazelcastAwsDiscoveryStrategyTests {
    @Test
    public void verifyAction() {
        final HazelcastAwsDiscoveryStrategy strategy = new HazelcastAwsDiscoveryStrategy();
        final HazelcastClusterProperties properties = new HazelcastClusterProperties();
        final HazelcastAwsDiscoveryProperties aws = properties.getDiscovery().getAws();

        aws.setAccessKey("AccessKey");
        aws.setSecretKey("Secret");
        aws.setIamRole("Role");
        aws.setHostHeader("Header");
        aws.setPort(1000);
        aws.setRegion("us-east-1");
        aws.setSecurityGroupName("Group");
        aws.setTagKey("TagKey");
        aws.setTagValue("TagValue");
        assertNotNull(strategy.get(properties));
    }
}
