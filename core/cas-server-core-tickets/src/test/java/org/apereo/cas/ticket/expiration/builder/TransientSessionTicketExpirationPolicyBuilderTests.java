package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TransientSessionTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TransientSessionTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class TransientSessionTicketExpirationPolicyBuilderTests {
    @Test
    public void verifyType() {
        val input = new TransientSessionTicketExpirationPolicyBuilder(new CasConfigurationProperties());
        assertEquals(TransientSessionTicket.class, input.getTicketType());
        assertNotNull(input.buildTicketExpirationPolicy());
    }
}
