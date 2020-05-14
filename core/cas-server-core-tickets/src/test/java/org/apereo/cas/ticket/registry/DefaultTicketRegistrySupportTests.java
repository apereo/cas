package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTicketRegistrySupportTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultTicketRegistrySupportTests {

    @Test
    public void verifyOperation() {
        val registry = new DefaultTicketRegistry();
        val tgt = new MockTicketGrantingTicket("casuser", Map.of("name", List.of("CAS")));
        registry.addTicket(tgt);
        val support = new DefaultTicketRegistrySupport(registry);
        assertNotNull(support.getTicketState(tgt.getId()));
        assertNull(support.getTicketState(null));
        assertFalse(support.getPrincipalAttributesFrom(tgt.getId()).isEmpty());

        val authn = CoreAuthenticationTestUtils.getAuthentication("new-authn", Map.of("newAuthN", List.of("CAS-new")));
        support.updateAuthentication(tgt.getId(), authn);
        assertTrue(registry.getTicket(tgt.getId(), TicketGrantingTicket.class)
            .getAuthentication().getAttributes().containsKey("newAuthN"));

    }
}
