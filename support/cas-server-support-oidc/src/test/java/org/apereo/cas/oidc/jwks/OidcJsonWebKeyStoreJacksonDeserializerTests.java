package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
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
        .defaultTypingEnabled(false).modules(List.of(new SimpleModule()
            .addDeserializer(JsonWebKeySet.class, new OidcJsonWebKeyStoreJacksonDeserializer())))
        .build().toObjectMapper();

    @Test
    void verifyOperation() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        assertNotNull(MAPPER.readValue(keyset, JsonWebKeySet.class));
    }

    @Test
    void verifyMaliciousKeyId() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        key.setKeyId("${T(java.lang.System).setProperty('casSpelInjectionProof','INJECTED')}");
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        assertThrows(IllegalArgumentException.class, () -> MAPPER.readValue(keyset, JsonWebKeySet.class));
    }

    @Test
    void verifyMaliciousKeyIdInlined() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        key.setKeyId("ThisIsMyKey-${T(java.lang.System).setProperty('casSpelInjectionProof','INJECTED')}");
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        assertThrows(IllegalArgumentException.class, () -> MAPPER.readValue(keyset, JsonWebKeySet.class));
    }

}
