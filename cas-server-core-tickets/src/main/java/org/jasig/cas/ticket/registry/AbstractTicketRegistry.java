package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry, TicketRegistryState {

    /** The Slf4j logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
    public final boolean deleteTicket(final String ticketId) {
        if (ticketId == null) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            deleteChildren((TicketGrantingTicket) ticket);
        }

        logger.debug("Removing ticket [{}] from the registry.", ticket);
        return deleteSingleTicket(ticketId);
    }


    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     */
    private void deleteChildren(final TicketGrantingTicket ticket) {
        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            for (final Map.Entry<String, Service> entry : services.entrySet()) {
                final String ticketId = entry.getKey();
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", entry.getKey());
                } else {
                    logger.debug("Unable to remove ticket [{}]", entry.getKey());
                }
            }
        }
        final Collection<ProxyGrantingTicket> proxyGrantingTickets = ticket.getProxyGrantingTickets();
        for (final ProxyGrantingTicket proxyGrantingTicket : proxyGrantingTickets) {
            logger.debug("Removing proxy-granting ticket [{}]", proxyGrantingTicket.getId());
            deleteTicket(proxyGrantingTicket.getId());
        }
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
