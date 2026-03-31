package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcVerifiableCredentialDefaultNonceServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
class OidcVerifiableCredentialDefaultNonceServiceTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcVerifiableCredentialNonceService")
    private OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

    @Test
    void verifyNonceCreation() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        assertNotNull(nonce);
        assertNotNull(nonce.value());
        assertNotNull(nonce.expiresAt());
    }

    @Test
    void verifyNonceExpiresInFuture() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        assertTrue(nonce.expiresAt().isAfter(Instant.now(Clock.systemUTC())));
    }

    @Test
    void verifyNonceExistsAfterCreation() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        assertTrue(oidcVerifiableCredentialNonceService.exists(nonce.value()));
    }

    @Test
    void verifyNonceDoesNotExistForUnknownValue() {
        assertFalse(oidcVerifiableCredentialNonceService.exists("TST-unknown-nonce-value"));
    }

    @Test
    void verifyNonceRemoval() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        assertTrue(oidcVerifiableCredentialNonceService.exists(nonce.value()));
        oidcVerifiableCredentialNonceService.remove(nonce.value());
        assertFalse(oidcVerifiableCredentialNonceService.exists(nonce.value()));
    }

    @Test
    void verifyRemovingNonExistentNonceDoesNotThrow() {
        assertDoesNotThrow(() -> oidcVerifiableCredentialNonceService.remove("TST-does-not-exist"));
    }

    @Test
    void verifyMultipleNoncesAreIndependent() {
        val nonce1 = oidcVerifiableCredentialNonceService.create();
        val nonce2 = oidcVerifiableCredentialNonceService.create();
        assertNotEquals(nonce1.value(), nonce2.value());
        assertTrue(oidcVerifiableCredentialNonceService.exists(nonce1.value()));
        assertTrue(oidcVerifiableCredentialNonceService.exists(nonce2.value()));

        oidcVerifiableCredentialNonceService.remove(nonce1.value());
        assertFalse(oidcVerifiableCredentialNonceService.exists(nonce1.value()));
        assertTrue(oidcVerifiableCredentialNonceService.exists(nonce2.value()));
    }

    @Test
    void verifyNonceValuePrefixMatchesTransientTicket() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        assertTrue(nonce.value().startsWith("TST-"));
    }

    @Test
    void verifyCreateAndRemoveMultipleTimes() {
        for (var i = 0; i < 5; i++) {
            val nonce = oidcVerifiableCredentialNonceService.create();
            assertTrue(oidcVerifiableCredentialNonceService.exists(nonce.value()));
            oidcVerifiableCredentialNonceService.remove(nonce.value());
            assertFalse(oidcVerifiableCredentialNonceService.exists(nonce.value()));
        }
    }

    @Test
    void verifyDoubleRemoveDoesNotThrow() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        oidcVerifiableCredentialNonceService.remove(nonce.value());
        assertDoesNotThrow(() -> oidcVerifiableCredentialNonceService.remove(nonce.value()));
    }

    @Test
    void verifyExistsReturnsFalseAfterRemoval() {
        val nonce = oidcVerifiableCredentialNonceService.create();
        val nonceValue = nonce.value();
        oidcVerifiableCredentialNonceService.remove(nonceValue);
        assertFalse(oidcVerifiableCredentialNonceService.exists(nonceValue));
        assertFalse(oidcVerifiableCredentialNonceService.exists(nonceValue));
    }
}
