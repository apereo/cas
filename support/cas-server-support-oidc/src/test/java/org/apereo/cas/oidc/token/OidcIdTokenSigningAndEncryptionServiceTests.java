package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
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
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        val jwt = oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService));
        assertNotNull(jwt);
    }
}
