package org.apereo.cas.ticket.registry;

/**
 * This is {@link TicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface TicketRegistryCleaner {

    /**
     * Clean the ticket registry by collecting
     * tickets in the storage unit that may be expired.
     */
    default void clean() {}
}
