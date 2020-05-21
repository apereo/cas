package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.scope.refresh.RefreshScope;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutor2Tests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcRegisteredServiceJwtAccessTokenCipherExecutor2Tests extends AbstractOidcTests {
    @Autowired
    @Qualifier("refreshScope")
    private RefreshScope refreshScope;

    @ParameterizedTest
    @ValueSource(strings = {
        "classpath:keystore.jwks",
        "classpath:keystore-p256.jwks",
        "classpath:keystore-p384.jwks",
        "classpath:keystore-p521.jwks"
    })
    public void verifyOperation(final String jwks) {
        setJwksFile(jwks);
        refreshScope.refresh("oidcJsonWebKeystoreGeneratorService");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCacheLoader");
        refreshScope.refresh("oidcDefaultJsonWebKeystoreCache");
        refreshScope.refresh("oidcTokenSigningAndEncryptionService");
        val service = getOidcRegisteredService("whatever");
        assertTrue(oauthRegisteredServiceJwtAccessTokenCipherExecutor.supports(service));
        val at = getAccessToken();
        val encoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.encode(at.getId(), Optional.of(service));
        assertNotNull(encoded);
        val decoded = oauthRegisteredServiceJwtAccessTokenCipherExecutor.decode(encoded, Optional.of(service));
        assertNotNull(decoded);
        assertEquals(at.getId(), decoded);
    }
}
