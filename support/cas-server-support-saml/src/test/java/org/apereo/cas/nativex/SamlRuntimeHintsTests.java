package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class SamlRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new SamlRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(SamlService.class).test(hints));
    }
}
