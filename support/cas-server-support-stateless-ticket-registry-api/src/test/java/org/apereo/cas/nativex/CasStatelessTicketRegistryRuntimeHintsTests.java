package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.compact.ServiceTicketCompactor;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasStatelessTicketRegistryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class CasStatelessTicketRegistryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasStatelessTicketRegistryRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(TicketCompactor.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(ServiceTicketCompactor.class).test(hints));
    }
}
