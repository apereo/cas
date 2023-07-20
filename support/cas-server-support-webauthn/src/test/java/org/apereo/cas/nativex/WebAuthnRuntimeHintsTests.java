package org.apereo.cas.nativex;

import org.apereo.cas.webauthn.WebAuthnCredentialRegistrationCipherExecutor;
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
    }
}
