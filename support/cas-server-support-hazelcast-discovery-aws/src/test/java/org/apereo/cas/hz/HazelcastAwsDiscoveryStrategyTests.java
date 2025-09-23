package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.test.CasTestExtension;

import com.hazelcast.aws.AwsDiscoveryStrategyFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastAwsDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class HazelcastAwsDiscoveryStrategyTests {
    @Test
    void verifyAction() {
        val strategy = new HazelcastAwsDiscoveryStrategy();
        val properties = new HazelcastClusterProperties();
        val aws = properties.getDiscovery().getAws();

        aws.setFamily("Family");
        aws.setCluster("Cluster");
        aws.setServiceName("MyService");
        aws.setAccessKey("AccessKey");
        aws.setSecretKey("Secret");
        aws.setIamRole("Role");
        aws.setHostHeader("Header");
        aws.setPort(1000);
        aws.setRegion("us-east-1");
        aws.setSecurityGroupName("Group");
        aws.setTagKey("TagKey");
        aws.setTagValue("TagValue");

        val result = strategy.get(properties, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
        assertTrue(result.isPresent());

        val discoveryProperties = result.get().getProperties();
        for (val propertyDefinition : new AwsDiscoveryStrategyFactory().getConfigurationProperties()) {
            val value = discoveryProperties.get(propertyDefinition.key());
            if (value == null) {
                assertTrue(propertyDefinition.optional(),
                    () -> "Property " + propertyDefinition.key() + " is not optional and should be given");
            } else {
                assertDoesNotThrow(() -> propertyDefinition.typeConverter().convert(value),
                    () -> "Property " + propertyDefinition.key() + " has invalid value '" + value + '\'');
            }
        }

    }
}
