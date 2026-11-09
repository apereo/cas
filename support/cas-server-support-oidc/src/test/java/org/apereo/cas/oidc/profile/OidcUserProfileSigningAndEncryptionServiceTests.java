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

    /**
     * User info for a service that is not an OpenID Connect service is never signed, and unsigned JWTs are not issued.
     */
    @Test
    void verifyOAuth() {
        val service = getOAuthRegisteredService("example", "https://example.org");
        assertThrows(IllegalStateException.class, () -> oidcUserProfileSigningAndEncryptionService.encode(service, getClaims()));
    }

    /**
     * A service that asks only for encryption gets user info signed with the default algorithm, then encrypted; one that
     * asks for {@code none} and no encryption gets no JWT at all.
     */
    @Test
    void verifyEncryptionOnlyIsSigned() throws Throwable {
        val service = getOidcRegisteredService();
        service.setUserInfoSigningAlg(null);
        service.setUserInfoEncryptedResponseEncoding(OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
        service.setUserInfoEncryptedResponseAlg("RSA-OAEP-256");
        assertTrue(oidcUserProfileSigningAndEncryptionService.shouldSignToken(service));
        val input = oidcUserProfileSigningAndEncryptionService.encode(service, getClaims());
        assertNotNull(oidcUserProfileSigningAndEncryptionService.decode(input, Optional.of(service)));

        service.setUserInfoSigningAlg("none");
        service.setUserInfoEncryptedResponseAlg(null);
        assertFalse(oidcUserProfileSigningAndEncryptionService.shouldSignToken(service));
        assertFalse(oidcUserProfileSigningAndEncryptionService.shouldEncryptToken(service));
        assertFalse(oidcServerDiscoverySettings.getUserInfoSigningAlgValuesSupported().contains("none"));
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
