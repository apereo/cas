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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class is testing that the conditional expression on the SamlIdPRedisIdPMetadataConfiguration class works.
 * @since 6.4.0
 */
@SpringBootTest(classes = SamlIdPRedisIdPMetadataConfiguration.class)
@TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.redis.idp-metadata-enabled=true",
        "cas.authn.saml-idp.metadata.redis.enabled=true"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ConditionalOnExpressionPositiveTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyConfigClassLoaded() {
        String[] beans = applicationContext.getBeanDefinitionNames();
        assertTrue(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataConnectionFactory"::equalsIgnoreCase));
        assertTrue(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataTemplate"::equalsIgnoreCase));
    }

}
