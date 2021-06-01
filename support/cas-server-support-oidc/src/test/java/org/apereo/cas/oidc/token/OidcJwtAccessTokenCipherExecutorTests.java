package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwtAccessTokenCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcJwtAccessTokenCipherExecutorTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() throws Exception {
        val at = getAccessToken();
        val encoded = oidcAccessTokenJwtCipherExecutor.encode(at.getId());
        assertNotNull(encoded);
        val header = SignedJWT.parse(encoded).getHeader();
        assertNotNull(header.getAlgorithm());
        val decoded = oidcAccessTokenJwtCipherExecutor.decode(encoded);
        assertNotNull(decoded);
        assertEquals(at.getId(), decoded);
    }
}
