package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * This is {@link NoOpTicketRegistryCleaner} that simply disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpTicketRegistryCleaner implements TicketRegistryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpTicketRegistryCleaner.class);

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
