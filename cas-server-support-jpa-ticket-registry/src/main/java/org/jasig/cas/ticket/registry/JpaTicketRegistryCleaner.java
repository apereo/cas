package org.jasig.cas.ticket.registry;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.Collection;

/**
 * This is {@link JpaTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 4.2.3
 */
@Transactional(transactionManager = "ticketTransactionManager", readOnly = true)
public class JpaTicketRegistryCleaner extends TransactionTemplate implements Job {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("jpaLockingStrategy")
    private LockingStrategy jpaLockingStrategy;
    
    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

            logger.info("Beginning ticket cleanup.");
            logger.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.jpaLockingStrategy.acquire()) {
                logger.info("Could not obtain lock.  Aborting cleanup.");
                return;
            }
            logger.debug("Acquired lock.  Proceeding with cleanup.");
            
            logger.info("Beginning ticket cleanup...");
            final Collection<Ticket> ticketsToRemove = Collections2.filter(ticketRegistry.getTickets(), new Predicate<Ticket>() {
                @Override
                public boolean apply(final Ticket ticket) {
                    return ticket.isExpired();
                }
            });
            logger.info("{} expired tickets found.", ticketsToRemove.size());

            for (final Ticket ticket : ticketsToRemove) {
                if (ticket instanceof TicketGrantingTicket) {
                    logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                    logoutManager.performLogout((TicketGrantingTicket) ticket);
                    ticketRegistry.deleteTicket(ticket.getId());
                } else if (ticket instanceof ServiceTicket) {
                    logger.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                    ticketRegistry.deleteTicket(ticket.getId());
                } else {
                    logger.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                }
            }
            logger.info("{} expired tickets removed.", ticketsToRemove.size());
            
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            this.jpaLockingStrategy.release();
            logger.info("Finished ticket cleanup.");
        }

    }
}
