package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.saml.BaseRedisSamlMetadataTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.redis.host=localhost",
    "cas.authn.saml-idp.metadata.redis.port=6379",
    "cas.authn.saml-idp.metadata.redis.idp-metadata-enabled=true"
})
@Tag("Redis")
@EnabledIfListeningOnPort(port = 6379)
class RedisSamlIdPMetadataLocatorTests extends BaseRedisSamlMetadataTests {

    @Autowired
    @Qualifier("redisSamlIdPMetadataTemplate")
    protected CasRedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate;

    @BeforeEach
    void setup() {
        val key = RedisSamlIdPMetadataLocator.CAS_PREFIX + '*';
        try (val keys = redisSamlIdPMetadataTemplate.scan(key, 0L)) {
            redisSamlIdPMetadataTemplate.delete(keys.collect(Collectors.toSet()));
        }
    }

    @Test
    void verifySigningKeyWithoutService() throws Throwable {
        assertNotNull(redisSamlIdPMetadataTemplate);
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
