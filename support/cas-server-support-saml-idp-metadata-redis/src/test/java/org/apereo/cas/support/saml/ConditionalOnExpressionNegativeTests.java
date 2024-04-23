package org.apereo.cas.support.saml;

import org.apereo.cas.config.CasSamlIdPRedisIdPMetadataAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class,
    CasSamlIdPRedisIdPMetadataAutoConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml1984",
    "CasFeatureModule.SAMLIdentityProvider.redis.enabled=false"
})
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
