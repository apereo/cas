package org.apereo.cas.support.saml;

import org.apereo.cas.config.SamlIdPRedisIdPMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is testing that the conditional expression on the SamlIdPRedisIdPMetadataConfiguration class works.
 * The class should not be created because one of the properties is false.
 *
 * @since 6.4.0
 */
@Tag("Redis")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    SamlIdPRedisIdPMetadataConfiguration.class
})
@TestPropertySource(properties = "CasFeatureModule.SAMLIdentityProvider.redis.enabled=false")
@EnabledIfListeningOnPort(port = 6379)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ConditionalOnExpressionNegativeTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyConfigClassNotLoaded() throws Throwable {
        val beans = applicationContext.getBeanDefinitionNames();
        assertFalse(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataConnectionFactory"::equalsIgnoreCase));
    }

}
