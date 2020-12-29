package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseRedisSamlMetadataTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

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
@EnabledIfPortOpen(port = 6379)
public class RedisSamlIdPMetadataLocatorTests extends BaseRedisSamlMetadataTests {

    @Autowired
    @Qualifier("redisSamlIdPMetadataTemplate")
    protected RedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate;

    @BeforeEach
    public void setup() {
        val key = RedisSamlIdPMetadataLocator.CAS_PREFIX + '*';
        val keys = redisSamlIdPMetadataTemplate.keys(key);
        if (keys != null) {
            redisSamlIdPMetadataTemplate.delete(keys);
        }
    }

    @Test
    public void verifySigningKeyWithoutService() {
        assertNotNull(redisSamlIdPMetadataTemplate);
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
