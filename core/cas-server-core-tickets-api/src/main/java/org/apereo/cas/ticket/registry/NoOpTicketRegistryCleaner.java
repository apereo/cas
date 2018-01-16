package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link NoOpTicketRegistryCleaner} that simply disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class NoOpTicketRegistryCleaner implements TicketRegistryCleaner {


    private static TicketRegistryCleaner INSTANCE;

    protected NoOpTicketRegistryCleaner() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static TicketRegistryCleaner getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoOpTicketRegistryCleaner();
        }
        return INSTANCE;
    }
}
