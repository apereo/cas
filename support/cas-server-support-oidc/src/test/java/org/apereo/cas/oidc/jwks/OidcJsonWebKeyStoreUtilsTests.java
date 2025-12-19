package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeyStoreUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
class OidcJsonWebKeyStoreUtilsTests extends AbstractOidcTests {

    @Test
    void verifyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(StringUtils.EMPTY);
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyBadSvc() {
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(
            null, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyEmptyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(new JsonWebKeySet(List.of()).toJson());
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyEc() {
        assertNotNull(OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING));
        assertNotNull(OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 256, OidcJsonWebKeyUsage.SIGNING));
    }

    @Test
    void verifyParsing() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key);
        assertNotNull(OidcJsonWebKeyStoreUtils.parseJsonWebKeySet(
            keyset.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)));
    }
}
