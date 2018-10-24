package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcDefaultJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        assertTrue(oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc").isPresent());
        assertTrue(oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc").isPresent());
    }
}
