package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import java.util.Iterator;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry, TicketRegistryState {

    /** The Slf4j logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if class is null.
     * @throws ClassCastException if class does not match requested ticket
     * class.
     * @return specified ticket from the registry
     */
    @Override
    public final <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");

        final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                + " is of type " + ticket.getClass()
                + " when we were expecting " + clazz);
        }

        return (T) ticket;
    }

    @Override
    public int sessionCount() {
      logger.debug("sessionCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Integer.MIN_VALUE);
      return Integer.MIN_VALUE;
    }

    @Override
    public int serviceTicketCount() {
      logger.debug("serviceTicketCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Integer.MIN_VALUE);
      return Integer.MIN_VALUE;
    }

    @Override
    public int deleteTicket(final String ticketId) {
        int count = 0;

        if (ticketId == null) {
            return count;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return count;
        }

        if (ticket instanceof TicketGrantingTicket) {
            if (ticket instanceof ProxyGrantingTicket) {
                logger.debug("Removing proxy-granting ticket [{}]", ticketId);
            }

            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            count += deleteChildren(tgt);

            final Collection<ProxyGrantingTicket> proxyGrantingTickets = tgt.getProxyGrantingTickets();
            final Iterator<ProxyGrantingTicket> it = proxyGrantingTickets.iterator();
            while(it.hasNext()) {
                final ProxyGrantingTicket pgt = it.next();
                count += deleteTicket(pgt.getId());
            }
        }
        logger.debug("Removing ticket [{}] from the registry.", ticket);

        if (deleteSingleTicket(ticketId)) {
            count++;
        }
 
        return count;
    }

    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     * @return the count of tickets that were removed including child tickets and zero if the ticket was not deleted
     */
    public int deleteChildren(final TicketGrantingTicket ticket) {
        int count = 0;

        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            final Iterator<String> it = services.keySet().iterator();

            while (it.hasNext()) {
                final String ticketId = it.next();
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", ticketId);
                    count++;
                } else {
                    logger.debug("Unable to remove ticket [{}]", ticketId);
                }
            }
        }

        return count;
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public boolean deleteSingleTicket(final Ticket ticketId) {
        return deleteSingleTicket(ticketId.getId());
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public abstract boolean deleteSingleTicket(final String ticketId);
}
