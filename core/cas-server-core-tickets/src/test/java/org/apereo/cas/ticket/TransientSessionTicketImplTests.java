package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TransientSessionTicketImplTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class TransientSessionTicketImplTests {
    @Test
    void verifyOperation() {
        val ticket = new TransientSessionTicketImpl("TST-1", NeverExpiresExpirationPolicy.INSTANCE,
            CoreAuthenticationTestUtils.getService(), Map.of("key", "value"));
        ticket.putProperty("key2", "value2");
        ticket.putAllProperties(Map.of("key3", "value3"));
        assertTrue(ticket.containsProperty("key2"));
        assertNull(ticket.getProperty("invalid", String.class));
        assertNotNull(ticket.getProperty("key3", String.class));
        assertNotNull(ticket.getProperty("something", String.class, "default"));
    }
}
