package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyRedisConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This class is testing that the conditional expression on the CasAcceptableUsagePolicyRedisConfiguration class works.
 * The positive test is done implicitly by other tests that use the CasAcceptableUsagePolicyRedisConfiguration class.
 * @since 6.4.0
 */
@Tag("Redis")
@SpringBootTest(classes = CasAcceptableUsagePolicyRedisConfiguration.class)
@TestPropertySource(properties = {
        "cas.acceptable-usage-policy.core.enabled=false",
        "cas.acceptable-usage-policy.redis.enabled=true"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ConditionalOnExpressionNegativeTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyConfigClassLoaded() {
        val beans = applicationContext.getBeanDefinitionNames();
        assertFalse(Arrays.stream(beans).anyMatch("redisAcceptableUsagePolicyTemplate"::equalsIgnoreCase));
        assertFalse(Arrays.stream(beans).anyMatch("redisAcceptableUsagePolicyConnectionFactory"::equalsIgnoreCase));
    }

}
