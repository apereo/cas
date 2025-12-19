package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
class ProxyGrantingTicketExpirationPolicyBuilderTests {
    @Test
    void verifyType() {
        val input = new ProxyGrantingTicketExpirationPolicyBuilder(
            new TicketGrantingTicketExpirationPolicyBuilder(new CasConfigurationProperties()),
            new CasConfigurationProperties());
        assertNotNull(input.buildTicketExpirationPolicy());
    }
}
