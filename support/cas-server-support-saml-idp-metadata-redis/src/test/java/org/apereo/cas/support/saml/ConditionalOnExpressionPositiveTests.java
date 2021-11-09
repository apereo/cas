package org.apereo.cas.support.saml;

import org.apereo.cas.config.SamlIdPRedisIdPMetadataConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is testing that the conditional expression on
 * the {@link SamlIdPRedisIdPMetadataConfiguration} class works.
 *
 * @since 6.4.0
 */
@Tag("Redis")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.redis.host=localhost",
    "cas.authn.saml-idp.metadata.redis.port=6379",
    "cas.authn.saml-idp.metadata.redis.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.redis.enabled=true"
})
@EnabledIfPortOpen(port = 6379)
public class ConditionalOnExpressionPositiveTests extends BaseRedisSamlMetadataTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyConfigClassLoaded() {
        val beans = applicationContext.getBeanDefinitionNames();
        assertTrue(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataConnectionFactory"::equalsIgnoreCase));
        assertTrue(Arrays.stream(beans).anyMatch("redisSamlIdPMetadataTemplate"::equalsIgnoreCase));
    }

}
