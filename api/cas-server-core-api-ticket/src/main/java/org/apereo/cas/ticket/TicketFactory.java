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
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get ticket factory.
     *
     * @param clazz the clazz
     * @return ticket factory object
     */
    default TicketFactory get(Class<? extends Ticket> clazz) {
        return this;
    }

    /**
     * Gets ticket type.
     *
     * @return the ticket type
     */
    Class<? extends Ticket> getTicketType();
}
