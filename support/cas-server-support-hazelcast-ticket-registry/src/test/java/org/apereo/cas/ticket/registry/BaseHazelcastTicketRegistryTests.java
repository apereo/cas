package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseHazelcastTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("SkipClearingTicketRegistry")
public abstract class BaseHazelcastTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    protected boolean isCipherExecutorOwnedByContext() {
        return true;
    }

    @Override
    @RepeatedTest(2)
    void verifyGetTicketsIsZero() throws Throwable {
        val principal = UUID.randomUUID().toString();
        addSessionFor(principal);
        getNewTicketRegistry().deleteTicketsFor(principal);
        assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
    }

    @Override
    @RepeatedTest(2)
    void verifyDeleteAllExistingTickets() throws Throwable {
        val principal = UUID.randomUUID().toString();
        addSessionFor(principal);
        assertEquals(1, getNewTicketRegistry().deleteTicketsFor(principal));
        assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
    }

    private void addSessionFor(final String principal) throws Throwable {
        val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(TicketGrantingTicket.PREFIX);
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(principal), NeverExpiresExpirationPolicy.INSTANCE));
    }
}
