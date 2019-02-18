package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void verifyValidationOperation() {
        val claims = getClaims();
        val result = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(true, false), claims);
        val jwt = oidcTokenSigningAndEncryptionService.validate(result);
        assertNotNull(jwt);
    }
}
