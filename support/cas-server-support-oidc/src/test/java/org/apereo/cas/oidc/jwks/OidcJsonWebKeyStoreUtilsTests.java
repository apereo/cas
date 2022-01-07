package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeyStoreUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcJsonWebKeyStoreUtilsTests extends AbstractOidcTests {

    @Test
    public void verifyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(StringUtils.EMPTY);
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, resourceLoader, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    public void verifyBadSvc() {
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(
            null, resourceLoader, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    public void verifyEmptyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(new JsonWebKeySet(List.of()).toJson());
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, resourceLoader, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    public void verifyEc() {
        assertNotNull(OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING));
        assertNotNull(OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 256, OidcJsonWebKeyUsage.SIGNING));
    }

    @Test
    public void verifyParsing() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key);
        assertNotNull(OidcJsonWebKeyStoreUtils.parseJsonWebKeySet(
            keyset.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)));
    }
}
