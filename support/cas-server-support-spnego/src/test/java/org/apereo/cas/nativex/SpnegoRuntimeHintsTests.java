package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpnegoRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class SpnegoRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new SpnegoRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(SpnegoCredential.class).test(hints));
    }
}
