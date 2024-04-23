package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.lock.LockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(transactionManager = "ticketTransactionManager")
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
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return 0;
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return lockRepository.execute(ticket.getId(), () -> {
            try {
                if (ticket instanceof final TicketGrantingTicket tgt) {
                    LOGGER.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                    val request = SingleLogoutExecutionRequest.builder()
                        .ticketGrantingTicket(tgt)
                        .build();
                    logoutManager.performLogout(request);
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }

            try {
                LOGGER.debug("Cleaning up expired ticket [{}]", ticket.getId());
                return ticketRegistry.deleteTicket(ticket);
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
                return 0;
            }
        }).orElse(0);
    }

    protected int cleanInternal() {
        try (val expiredTickets = ticketRegistry.stream().filter(Objects::nonNull).filter(Ticket::isExpired)) {
            val ticketsDeleted = expiredTickets.mapToInt(this::cleanTicket).sum();
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
