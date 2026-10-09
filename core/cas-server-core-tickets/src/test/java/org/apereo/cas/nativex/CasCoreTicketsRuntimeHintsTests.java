package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirableTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.StatelessTicket;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.DecoratingProxy;
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
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasCoreTicketsRuntimeHints().registerHints(hints, getClass().getClassLoader());
        val scheduler = Class.forName("org.apereo.cas.config.CasCoreTicketsSchedulingConfiguration$TicketRegistryCleanerScheduler");
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(scheduler.getDeclaredMethod("run")).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(DisposableBean.class, TicketRegistry.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(TicketFactoryExecutionPlanConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(TicketGrantingCookieCipherExecutor.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(TicketGrantingTicketImpl.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(ServiceTicketImpl.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(TimeoutExpirationPolicy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(AbstractTicket.class.getMethod("getExpirationPolicy")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(ExpirableTicket.class.getMethod("getExpirationPolicy")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(StatelessTicket.class.getMethod("isStateless")).test(hints));
    }
}
