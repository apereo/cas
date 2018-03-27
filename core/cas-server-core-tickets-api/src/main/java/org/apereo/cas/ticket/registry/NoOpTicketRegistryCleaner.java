package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;
import lombok.NoArgsConstructor;

/**
 * This is {@link NoOpTicketRegistryCleaner} that simply disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@NoArgsConstructor
public class NoOpTicketRegistryCleaner implements TicketRegistryCleaner {

    private static TicketRegistryCleaner INSTANCE;

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
