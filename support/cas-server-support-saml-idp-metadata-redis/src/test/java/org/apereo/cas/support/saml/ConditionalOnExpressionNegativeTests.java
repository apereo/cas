package org.apereo.cas.support.saml;

import org.apereo.cas.config.SamlIdPRedisIdPMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This class is testing that the conditional expression on the SamlIdPRedisIdPMetadataConfiguration class works.
 * The class should not be created because one of the properties is false.
 * @since 6.4.0
 */
@SpringBootTest(classes = SamlIdPRedisIdPMetadataConfiguration.class)
@TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.redis.idp-metadata-enabled=true",
        "cas.acceptable-usage-policy.core.redis.enabled=false"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ConditionalOnExpressionNegativeTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyConfigClassNotLoaded() {
        String[] beans = applicationContext.getBeanDefinitionNames();
        assertFalse(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataConnectionFactory"::equalsIgnoreCase));
    }

}
