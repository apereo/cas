package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public class DefaultTicketRegistryCleaner implements TicketRegistryCleaner, Serializable {
    private static final long serialVersionUID = -8581398063126547772L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTicketRegistryCleaner.class);

    private final LogoutManager logoutManager;
    private final TicketRegistry ticketRegistry;
    private final LockingStrategy lockingStrategy;

    public DefaultTicketRegistryCleaner(final LockingStrategy lockingStrategy,
                                        final LogoutManager logoutManager,
                                        final TicketRegistry ticketRegistry) {
        this.lockingStrategy = lockingStrategy;
        this.logoutManager = logoutManager;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public void clean() {
        try {
            if (!isCleanerSupported()) {
                LOGGER.trace("Ticket registry cleaner is not supported by [{}]. No cleaner processes will run.",
                        getClass().getSimpleName());
                return;
            }

            LOGGER.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.lockingStrategy.acquire()) {
                LOGGER.info("Could not obtain lock. Aborting cleanup. The ticket registry may not support self-service maintenance.");
                return;
            }
            LOGGER.debug("Acquired lock. Proceeding with cleanup.");
            cleanInternal();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.debug("Releasing ticket cleanup lock.");
            this.lockingStrategy.release();
            LOGGER.debug("Finished ticket cleanup.");
        }
    }

    /**
     * Clean tickets.
     */
    protected void cleanInternal() {
        final int ticketsDeleted = ticketRegistry.getTicketsStream()
                .filter(Ticket::isExpired)
                .mapToInt(this::cleanTicket)
                .sum();
        LOGGER.info("[{}] expired tickets removed.", ticketsDeleted);
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        if (ticket instanceof TicketGrantingTicket) {
            LOGGER.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
            logoutManager.performLogout((TicketGrantingTicket) ticket);
            return ticketRegistry.deleteTicket(ticket.getId());
        }
        if (ticket instanceof ServiceTicket) {
            LOGGER.debug("Cleaning up expired service ticket [{}]", ticket.getId());
            return ticketRegistry.deleteTicket(ticket.getId());
        }
        LOGGER.warn("Unknown ticket type [{}] found to clean", ticket.getClass().getSimpleName());
        return 0;
    }

    /**
     * Indicates whether the registry supports automated ticket cleanup.
     * Generally, a registry that is able to return a collection of available
     * tickets should be able to support the cleanup process. Default is {@code true}.
     *
     * @return true/false.
     */
    protected boolean isCleanerSupported() {
        return true;
    }
}
