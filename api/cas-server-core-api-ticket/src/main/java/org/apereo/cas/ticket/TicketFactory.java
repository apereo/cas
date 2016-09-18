package org.apereo.cas.ticket;

/**
 * The {@link TicketFactory} is an abstraction that decides
 * how CAS ticket factory objects are created.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface TicketFactory {

    /**
     * Get ticket factory.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return ticket factory object
     */
    <T extends TicketFactory> T get(Class<? extends Ticket> clazz);
}
