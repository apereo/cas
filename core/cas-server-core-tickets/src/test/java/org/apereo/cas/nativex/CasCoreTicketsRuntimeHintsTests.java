package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreTicketsRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreTicketsRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreTicketsRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(TicketFactoryExecutionPlanConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(TicketGrantingCookieCipherExecutor.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(TicketGrantingTicketImpl.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(ServiceTicketImpl.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(TimeoutExpirationPolicy.class).test(hints));
    }
}
