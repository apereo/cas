package org.apereo.cas.nativex;

import org.apereo.cas.api.PasswordlessUserAccount;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessAuthenticationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class PasswordlessAuthenticationRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new PasswordlessAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(PasswordlessUserAccount.class).test(hints));
    }
}
