package org.apereo.cas.ticket;

/**
 * The {@link TicketFactory} is an abstraction that decides
 * how CAS ticket factory objects are created.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@FunctionalInterface
public interface TicketFactory {

    /**
     * Get ticket factory.
     *
     * @param clazz the clazz
     * @return ticket factory object
     */
    TicketFactory get(Class<? extends Ticket> clazz);
}
