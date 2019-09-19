package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OidcRegisteredServiceJwtAccessTokenCipherExecutorTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
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
