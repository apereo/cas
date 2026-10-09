package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.webauthn.WebAuthnCredentialRegistrationCipherExecutor;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.core.SessionManager;
import com.yubico.webauthn.attestation.AttestationTrustSource;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class WebAuthnRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new WebAuthnRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(WebAuthnCredentialRegistrationCipherExecutor.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(WebAuthnCredentialRepository.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(SessionManager.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(AttestationTrustSource.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(MultifactorAuthenticationProvider.class).test(hints));
    }
}
