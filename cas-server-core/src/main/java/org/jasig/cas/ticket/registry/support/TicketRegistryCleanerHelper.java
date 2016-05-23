package org.jasig.cas.ticket.registry.support;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TicketRegistryCleanerHelper {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public int deleteExpiredTickets(final TicketRegistry ticketRegistry, final Collection<Ticket> ticketsInCache, final boolean logUserOutOfServices)
    {
        int numTicketsDeleted = 0;

        final List<Ticket> ticketsToRemove = new ArrayList<Ticket>();
        for (final Ticket ticket : ticketsInCache)
        {
            if (ticket.isExpired())
            {
                ticketsToRemove.add(ticket);
            }
        }

        log.info(ticketsToRemove.size() + " tickets found to be removed.");
        for (final Ticket ticket : ticketsToRemove)
        {
            // CAS-686: Expire TGT to trigger single sign-out
            if (logUserOutOfServices && ticket instanceof TicketGrantingTicket)
            {
                ((TicketGrantingTicket) ticket).expire();
            }
            ticketRegistry.deleteTicket(ticket.getId());
            numTicketsDeleted++;
        }

        return numTicketsDeleted;
    }

}
