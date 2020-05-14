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
@Tag("Simple")
public class TransientSessionTicketImplTests {
    @Test
    public void verifyOperation() {
        val ticket = new TransientSessionTicketImpl("TST-1", NeverExpiresExpirationPolicy.INSTANCE,
            CoreAuthenticationTestUtils.getService(), Map.of("key", "value"));
        ticket.put("key2", "value2");
        ticket.putAll(Map.of("key3", "value3"));
        assertTrue(ticket.contains("key2"));
        assertNull(ticket.get("invalid", String.class));
        assertNotNull(ticket.get("key3", String.class));
        assertNotNull(ticket.get("something", String.class, "default"));
    }
}
