package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRegistration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaWebAuthnRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JpaWebAuthnRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JpaWebAuthnRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(JpaWebAuthnCredentialRegistration.class.getDeclaredConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(JpaWebAuthnCredentialRegistration.class.getDeclaredField("id")).test(hints));
    }
}
