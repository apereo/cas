package org.apereo.cas.oidc.profile;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUserProfileSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.discovery.user-info-encryption-alg-values-supported=none,RSA1_5,RSA-OAEP-256")
class OidcUserProfileSigningAndEncryptionServiceTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val service = getOidcRegisteredService();
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("RSA-OAEP-256");
        service.setUserInfoSigningAlg("RS256");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
        assertDoesNotThrow(() -> {
            oidcUserProfileSigningAndEncryptionService.decode(input, Optional.of(service));
        });
    }

    @Test
    void verifyOAuth() throws Throwable {
        val service = getOAuthRegisteredService("example", "https://example.org");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
    }

    @Test
    void verifyNoSigningEncryption() throws Throwable {
        val service = getOidcRegisteredService();
        service.setUserInfoSigningAlg("RS256");
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("none");
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertFalse(input.isEmpty());
    }
}
