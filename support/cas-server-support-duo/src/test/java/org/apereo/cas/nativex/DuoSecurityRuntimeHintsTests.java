package org.apereo.cas.nativex;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class DuoSecurityRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new DuoSecurityRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(DuoSecurityUniversalPromptCredential.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(DuoSecurityPasscodeCredential.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(DuoSecurityUniversalPromptCredential.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(DuoSecurityPasscodeCredential.class).test(hints));
    }
}
