package org.apereo.cas.oidc.profile;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcUserProfileSigningAndEncryptionServiceTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val service = getOidcRegisteredService();
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("RSA-OAEP-256");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
    }

    @Test
    public void verifyOAuth() {
        val service = getOAuthRegisteredService("example", "https://example.org");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
    }

    @Test
    public void verifyNoSigningEncryption() {
        val service = getOidcRegisteredService();
        service.setUserInfoSigningAlg("RS256");
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("none");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
    }
}
