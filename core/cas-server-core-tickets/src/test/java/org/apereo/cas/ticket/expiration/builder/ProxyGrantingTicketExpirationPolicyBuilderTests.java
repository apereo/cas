package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ProxyGrantingTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class ProxyGrantingTicketExpirationPolicyBuilderTests {
    @Test
    public void verifyType() {
        val input = new ProxyGrantingTicketExpirationPolicyBuilder(
            new TicketGrantingTicketExpirationPolicyBuilder(new CasConfigurationProperties()),
            new CasConfigurationProperties());
        assertEquals(ProxyGrantingTicket.class, input.getTicketType());
        assertNotNull(input.buildTicketExpirationPolicy());
    }
}
