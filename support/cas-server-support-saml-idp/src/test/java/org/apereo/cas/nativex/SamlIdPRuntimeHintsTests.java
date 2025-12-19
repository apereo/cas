package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class SamlIdPRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new SamlIdPRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(SamlRegisteredService.class).test(hints));
    }
}
