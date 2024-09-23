package org.apereo.cas.ticket.registry;

import lombok.NoArgsConstructor;

/**
 * This is {@link NoOpTicketRegistryCleaner} that disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
public class NoOpTicketRegistryCleaner implements TicketRegistryCleaner {

    private static TicketRegistryCleaner INSTANCE;

    /**
     * Gets instance.
     *
     * @param ticketRegistry the ticket registry
     * @return the instance
     */
    public static TicketRegistryCleaner getInstance(final TicketRegistry ticketRegistry) {
        ticketRegistry.setCleanerEnabled(false);
        if (INSTANCE == null) {
            INSTANCE = new NoOpTicketRegistryCleaner();
        }
        return INSTANCE;
    }
}
