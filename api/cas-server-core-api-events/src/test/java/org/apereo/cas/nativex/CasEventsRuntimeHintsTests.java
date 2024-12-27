package org.apereo.cas.nativex;

import org.apereo.cas.config.CasConfigurationCreatedEvent;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEventsRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class CasEventsRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasEventsRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(CasConfigurationCreatedEvent.class).test(hints));
    }
}
