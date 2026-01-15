package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMultitenancyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class CasMultitenancyRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasMultitenancyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(TenantDefinition.class).test(hints));
    }
}
