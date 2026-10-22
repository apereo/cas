package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.HazelcastTicketDocumentRemovalProcessor;
import org.apereo.cas.ticket.registry.HazelcastTicketDocumentSerializer;
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
        assertTrue(RuntimeHintsPredicates.reflection().onType(HazelcastTicketDocumentSerializer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(HazelcastTicketDocumentRemovalProcessor.class).test(hints));
    }
}
