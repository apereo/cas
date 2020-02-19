package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks-type=ec",
    "cas.authn.oidc.jwks-key-size=384",
    "cas.authn.oidc.jwksFile=file:${#systemProperties['java.io.tmpdir']}/keystore.jwks"
})
public class OidcDefaultJsonWebKeystoreCacheLoaderTests extends AbstractOidcTests {
    @BeforeAll
    public static void setup() {
        val file = new File(System.getProperty("java.io.tmpdir"), "keystore.jwks");
        file.delete();
    }

    @Test
    public void verifyOperation() {
        val publicJsonWebKey1 = oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc");
        assertNotNull(publicJsonWebKey1);
        assertTrue(publicJsonWebKey1.isPresent());

        val publicJsonWebKey2 = oidcDefaultJsonWebKeystoreCache.get("https://sso.example.org/cas/oidc");
        assertNotNull(publicJsonWebKey2);
        assertTrue(publicJsonWebKey2.isPresent());
    }
}
