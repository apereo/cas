package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Pac4jCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class Pac4jCoreRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new Pac4jCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(DelegatedAuthenticationCandidateProfile.class).test(hints));
    }
}
