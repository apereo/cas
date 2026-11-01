package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.PulsarTicketRegistryMessageListener;
import lombok.val;
import org.apache.pulsar.client.impl.ConsumerImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.util.ReflectionUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PulsarTicketRegistryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class PulsarTicketRegistryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new PulsarTicketRegistryRuntimeHints().registerHints(hints, getClass().getClassLoader());
        val method = ReflectionUtils.findMethod(PulsarTicketRegistryMessageListener.class, "receive", String.class);
        assertNotNull(method);
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(method).test(hints));
        val configuration = ReflectionUtils.findField(ConsumerImpl.class, "conf");
        assertNotNull(configuration);
        assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(configuration).test(hints));
    }
}
