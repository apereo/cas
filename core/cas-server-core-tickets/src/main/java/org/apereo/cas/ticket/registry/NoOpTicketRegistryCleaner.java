package org.apereo.cas.ticket.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * This is {@link NoOpTicketRegistryCleaner} that simply disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpTicketRegistryCleaner implements TicketRegistryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpTicketRegistryCleaner.class);

    /**
     * Initialize cleaner.
     */
    @PostConstruct
    public void init() {
        LOGGER.debug("Ticket registry cleaner is a no-op task. No ticket cleaning will take place");
    }
}
