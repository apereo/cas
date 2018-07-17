package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcIdTokenSigningAndEncryptionServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() {
        val claims = getClaims();
        val result = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), claims);
        assertNotNull(result);
    }
}
