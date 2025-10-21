package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeyStoreJacksonDeserializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcJsonWebKeyStoreJacksonDeserializerTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val module = new SimpleModule();
        module.addDeserializer(JsonWebKeySet.class, new OidcJsonWebKeyStoreJacksonDeserializer());
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).modules(List.of(module)).build().toObjectMapper();
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("rsa", 2048, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        assertNotNull(mapper.readValue(keyset, JsonWebKeySet.class));
    }

}
