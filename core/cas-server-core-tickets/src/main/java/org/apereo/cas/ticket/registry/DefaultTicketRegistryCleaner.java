package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public class DefaultTicketRegistryCleaner implements TicketRegistryCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTicketRegistryCleaner.class);

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private LogoutManager logoutManager;

    private TicketRegistry ticketRegistry;
    
    private LockingStrategy lockingStrategy;
    
    @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.startDelay:20000}",
               fixedDelayString = "${cas.ticket.registry.cleaner.repeatInterval:60000}")
    @Override
    public void clean() {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            if (!casProperties.getTicket().getRegistry().getCleaner().isEnabled()) {
                LOGGER.trace("Ticket registry cleaner is disabled for {}. No cleaner processes will run.",
                        this.ticketRegistry.getClass().getSimpleName());
                return;
            }

            if (!isCleanerSupported()) {
                LOGGER.trace("Ticket registry cleaner is not supported by {}. No cleaner processes will run.",
                        getClass().getSimpleName());
                return;
            }

            LOGGER.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.lockingStrategy.acquire()) {
                LOGGER.info("Could not obtain lock. Aborting cleanup. The ticket registry may not support self-service maintenance.");
                return;
            }
            LOGGER.debug("Acquired lock.  Proceeding with cleanup.");
           
            final Collection<Ticket> ticketsToRemove = ticketRegistry.getTickets()
                    .stream()
                    .filter(Ticket::isExpired)
                    .collect(Collectors.toSet());
            LOGGER.debug("{} expired tickets found.", ticketsToRemove.size());

            int count = 0;

            for (final Ticket ticket : ticketsToRemove) {
                if (ticket instanceof TicketGrantingTicket) {
                    LOGGER.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                    logoutManager.performLogout((TicketGrantingTicket) ticket);
                    count += ticketRegistry.deleteTicket(ticket.getId());
                } else if (ticket instanceof ServiceTicket) {
                    LOGGER.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                    count += ticketRegistry.deleteTicket(ticket.getId());
                } else {
                    LOGGER.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                }
            }
            LOGGER.info("{} expired tickets removed.", count);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.debug("Releasing ticket cleanup lock.");
            this.lockingStrategy.release();
            LOGGER.debug("Finished ticket cleanup.");
        }
    }

    /**
     * Indicates whether the registry supports automatic ticket cleanup.
     * Generally, a registry that is able to return a collection of available
     * tickets should be able to support the cleanup process. Default is <code>true</code>.
     *
     * @return true/false.
     */
    protected boolean isCleanerSupported() {
        return true;
    }

    public void setLogoutManager(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setLockingStrategy(final LockingStrategy lockingStrategy) {
        this.lockingStrategy = lockingStrategy;
    }
}
