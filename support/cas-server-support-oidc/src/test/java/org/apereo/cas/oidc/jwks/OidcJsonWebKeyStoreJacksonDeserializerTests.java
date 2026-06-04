package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeyStoreJacksonDeserializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcJsonWebKeyStoreJacksonDeserializerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);

        val module = new SimpleModule();
        module.addDeserializer(JsonWebKeySet.class, new OidcJsonWebKeyStoreJacksonDeserializer());
        MAPPER.registerModule(module);
        assertNotNull(MAPPER.readValue(keyset, JsonWebKeySet.class));
    }

    @Test
    void verifyMaliciousKeyIdInlined() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        key.setKeyId("ThisIsMyKey-${T(java.lang.System).setProperty('casSpelInjectionProof','INJECTED')}");
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        assertThrows(IllegalArgumentException.class, () -> MAPPER.readValue(keyset, JsonWebKeySet.class));
    }
}
