package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastAwsDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HazelcastAwsDiscoveryStrategyTests {
    @Test
    public void verifyAction() {
        val strategy = new HazelcastAwsDiscoveryStrategy();
        val properties = new HazelcastClusterProperties();
        val aws = properties.getDiscovery().getAws();

        aws.setAccessKey("AccessKey");
        aws.setSecretKey("Secret");
        aws.setIamRole("Role");
        aws.setHostHeader("Header");
        aws.setPort(1000);
        aws.setRegion("us-east-1");
        aws.setSecurityGroupName("Group");
        aws.setTagKey("TagKey");
        aws.setTagValue("TagValue");

        assertNotNull(strategy.get(properties, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class)));
    }
}
