package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.lock.LockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketRegistryCleaner implements TicketRegistryCleaner {
    private final LockRepository lockRepository;

    private final LogoutManager logoutManager;

    private final TicketRegistry ticketRegistry;

    @Override
    public int clean() {
        try {
            if (!isCleanerSupported()) {
                LOGGER.trace("Ticket registry cleaner is not supported by [{}]", getClass().getSimpleName());
                return 0;
            }
            return cleanInternal();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return 0;
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return this.lockRepository.execute(ticket.getId(), Unchecked.supplier(() -> {
            if (ticket instanceof TicketGrantingTicket) {
                LOGGER.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                logoutManager.performLogout(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket((TicketGrantingTicket) ticket)
                    .build());
            }
            LOGGER.debug("Cleaning up expired ticket [{}]", ticket.getId());
            return ticketRegistry.deleteTicket(ticket);
        })).orElseThrow();
    }

    /**
     * Clean tickets.
     *
     * @return the int
     */
    protected int cleanInternal() {
        try (val expiredTickets = ticketRegistry.stream().filter(Ticket::isExpired)) {
            val ticketsDeleted = expiredTickets
                .mapToInt(this::cleanTicket)
                .sum();
            LOGGER.info("[{}] expired tickets removed.", ticketsDeleted);
            return ticketsDeleted;
        }
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
