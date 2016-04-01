package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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
}
