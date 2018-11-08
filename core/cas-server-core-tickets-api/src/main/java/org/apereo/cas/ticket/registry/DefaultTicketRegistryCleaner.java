package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.support.LockingStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketRegistryCleaner implements TicketRegistryCleaner, Serializable {
    private static final long serialVersionUID = -8581398063126547772L;

    private final transient LockingStrategy lockingStrategy;
    private final transient LogoutManager logoutManager;
    private final transient TicketRegistry ticketRegistry;

    @Override
    public void clean() {
        try {
            if (!isCleanerSupported()) {
                LOGGER.trace("Ticket registry cleaner is not supported by [{}]. No cleaner processes will run.", getClass().getSimpleName());
                return;
            }

            LOGGER.trace("Attempting to acquire ticket cleanup lock.");
            if (!this.lockingStrategy.acquire()) {
                LOGGER.info("Could not obtain lock. Aborting cleanup. The ticket registry may not support self-service maintenance.");
                return;
            }
            LOGGER.trace("Acquired lock. Proceeding with cleanup.");
            cleanInternal();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.trace("Releasing ticket cleanup lock.");
            this.lockingStrategy.release();
            LOGGER.debug("Finished ticket cleanup.");
        }
    }

    /**
     * Clean tickets.
     */
    protected void cleanInternal() {
        val ticketsDeleted = ticketRegistry.getTicketsStream()
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
        }
        LOGGER.debug("Cleaning up expired service ticket [{}]", ticket.getId());
        return ticketRegistry.deleteTicket(ticket.getId());
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
