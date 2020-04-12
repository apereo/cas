package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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

    @Test
    public void verifyDecodingFailureBadToken() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode("bad-token", Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureNoIssuer() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setIssuer(StringUtils.EMPTY);
        val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureBadIssuer() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setIssuer("bad-issuer");
        val result2 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result2, Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureBadClient() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, StringUtils.EMPTY);
        val result3 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result3, Optional.of(oidcRegisteredService)));
    }
}
