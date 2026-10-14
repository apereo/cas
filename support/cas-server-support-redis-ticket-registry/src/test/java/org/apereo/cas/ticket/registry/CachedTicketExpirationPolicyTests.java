package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachedTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Redis")
class CachedTicketExpirationPolicyTests {
    private final CachedTicketExpirationPolicy policy = new CachedTicketExpirationPolicy();

    @Test
    void verifyLongLivedTicketIsCappedOnCreateAndUpdate() {
        val ticket = new MockTicketGrantingTicket("casuser");
        ticket.setExpirationPolicy(NeverExpiresExpirationPolicy.INSTANCE);
        val maximum = CachedTicketExpirationPolicy.MAXIMUM_CACHE_DURATION.toNanos();
        assertEquals(maximum, policy.expireAfterCreate(ticket.getId(), ticket, 0));
        assertEquals(maximum, policy.expireAfterUpdate(ticket.getId(), ticket, 0, Long.MAX_VALUE));
    }

    @Test
    void verifyShortLivedTicketKeepsItsLifetime() {
        val ticket = new MockTicketGrantingTicket("casuser");
        ticket.setExpirationPolicy(new HardTimeoutExpirationPolicy(5));
        assertEquals(Duration.ofSeconds(5).toNanos(), policy.expireAfterCreate(ticket.getId(), ticket, 0));
    }

    @Test
    void verifyExpiredTicketIsNotKept() {
        val ticket = new MockTicketGrantingTicket("casuser");
        ticket.setExpirationPolicy(new HardTimeoutExpirationPolicy(5));
        ticket.markTicketExpired();
        assertEquals(0, policy.expireAfterCreate(ticket.getId(), ticket, 0));
        assertEquals(0, policy.expireAfterUpdate(ticket.getId(), ticket, 0, Long.MAX_VALUE));
    }

    @Test
    void verifyReadKeepsRemainingTime() {
        val ticket = new MockTicketGrantingTicket("casuser");
        val remaining = Duration.ofSeconds(7).toNanos();
        assertEquals(remaining, policy.expireAfterRead(ticket.getId(), ticket, 0, remaining));
    }
}
