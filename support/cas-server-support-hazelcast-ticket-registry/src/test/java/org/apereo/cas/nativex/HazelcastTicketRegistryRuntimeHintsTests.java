package org.apereo.cas.nativex;

import org.apereo.cas.ticket.registry.MapAttributeValueExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastTicketRegistryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class HazelcastTicketRegistryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new HazelcastTicketRegistryRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(MapAttributeValueExtractor.class).test(hints));
    }
}
