package org.jasig.cas.ticket.registry;

import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.joda.time.DateTime;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.2.1
 */
@Component("jpaTicketRegistry")
public final class JpaTicketRegistry extends AbstractTicketRegistry implements Job {

    @Value("${ticket.registry.cleaner.repeatinterval:5000}")
    private int refreshInterval;

    @Value("${ticket.registry.cleaner.startdelay:20}")
    private int startDelay;

    @Autowired
    @NotNull
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("jpaLockingStrategy")
    private LockingStrategy jpaLockingStrategy;

    @Value("${ticketreg.database.jpa.locking.tgt.enabled:true}")
    private boolean lockTgt = true;

    @NotNull
    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void updateTicket(final Ticket ticket) {
        entityManager.merge(ticket);
        logger.debug("Updated ticket [{}].", ticket);
    }

    @Override
    public void addTicket(final Ticket ticket) {
        entityManager.persist(ticket);
        logger.debug("Added ticket [{}] to registry.", ticket);
    }

    /**
     * Removes the ticket.
     *
     * @param ticket the ticket
     */
    private boolean removeTicket(final Ticket ticket) {
        try {
            final Date creationDate = new Date(ticket.getCreationTime());
            logger.debug("Removing Ticket [{}] created: {}", ticket, creationDate.toString());
            entityManager.remove(ticket);
            return true;
        } catch (final Exception e) {
            logger.error("Error removing {} from registry.", ticket.getId(), e);
        }
        return false;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return getProxiedTicketInstance(getRawTicket(ticketId));
    }

    /**
     * Gets the ticket from the database, as is.
     *
     * @param ticketId the ticket id
     * @return the raw ticket
     */
    private Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                    || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
                // There is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
                return entityManager.find(TicketGrantingTicketImpl.class, ticketId,
                        lockTgt ? LockModeType.PESSIMISTIC_WRITE : null);
            }

            return entityManager.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            logger.error("Error getting ticket {} from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = entityManager
                .createQuery("select t from TicketGrantingTicketImpl t", TicketGrantingTicketImpl.class)
                .getResultList();
        final List<ServiceTicketImpl> sts = entityManager
                .createQuery("select s from ServiceTicketImpl s", ServiceTicketImpl.class)
                .getResultList();

        final List<Ticket> tickets = new ArrayList<>();
        tickets.addAll(tgts);
        tickets.addAll(sts);

        return tickets;
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    @Override
    public int sessionCount() {
        return countToInt(entityManager.createQuery(
                "select count(t) from TicketGrantingTicketImpl t").getSingleResult());
    }

    @Override
    public int serviceTicketCount() {
        return countToInt(entityManager.createQuery("select count(t) from ServiceTicketImpl t").getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        int failureCount = 0;

        if (ticket instanceof ServiceTicket) {
            failureCount = deleteServiceTickets(ticketId);
        } else if (ticket instanceof TicketGrantingTicket) {
            failureCount = deleteTicketGrantingTickets(ticketId);
        } else {
            throw new IllegalArgumentException("Invalid ticket type with id " + ticketId);
        }
        return failureCount == 0;
    }

    <T extends Ticket> List<T> getTicketQueryResultList(final String ticketId, final String query, final Class<? extends Ticket> clazz) {
        return (List) entityManager.createQuery(query, clazz)
                .setParameter("id", ticketId)
                .getResultList();
    }

    private int deleteServiceTickets(final String ticketId) {
        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from ServiceTicketImpl s where s.id = :id", ServiceTicketImpl.class);
        return deleteTicketsFromResultList(serviceTicketImpls);
    }

    private int deleteTicketsFromResultList(final List<? extends Ticket> serviceTicketImpls) {
        int failureCount = 0;
        for (final Ticket serviceTicketImpl : serviceTicketImpls) {
            if (!removeTicket(serviceTicketImpl)) {
                failureCount++;
            }
        }
        return failureCount;
    }

    private int deleteTicketGrantingTickets(final String ticketId) {
        int failureCount = 0;

        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from ServiceTicketImpl s where s.ticketGrantingTicket.id = :id", ServiceTicketImpl.class);
        failureCount += deleteTicketsFromResultList(serviceTicketImpls);

        List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from TicketGrantingTicketImpl t where t.ticketGrantingTicket.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from TicketGrantingTicketImpl t where t.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        return failureCount;
    }

    /**
     * Count the result into a numeric value.
     *
     * @param result the result
     * @return the int
     */
    private static int countToInt(final Object result) {
        final int intval;
        if (result instanceof Long) {
            intval = ((Long) result).intValue();
        } else if (result instanceof Integer) {
            intval = (Integer) result;
        } else {
            // Must be a Number of some kind
            intval = ((Number) result).intValue();
        }
        return intval;
    }


    /**
     * Schedule reloader job.
     */
    @PostConstruct
    public void scheduleCleanerJob() {
        try {
            if (shouldScheduleCleanerJob()) {
                logger.info("Preparing to schedule cleaner job");
                final JobDetail job = JobBuilder.newJob(this.getClass())
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .build();

                final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .startAt(DateTime.now().plusSeconds(this.startDelay).toDate())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(this.refreshInterval)
                        .repeatForever()).build();

                logger.debug("Scheduling {} job", this.getClass().getName());
                scheduler.scheduleJob(job, trigger);
                logger.info("{} will clean tickets every {} seconds",
                    this.getClass().getSimpleName(),
                    TimeUnit.MILLISECONDS.toSeconds(this.refreshInterval));
            }
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {

            logger.info("Beginning ticket cleanup.");
            logger.debug("Attempting to acquire ticket cleanup lock.");
            if (!this.jpaLockingStrategy.acquire()) {
                logger.info("Could not obtain lock.  Aborting cleanup.");
                return;
            }
            logger.debug("Acquired lock.  Proceeding with cleanup.");

            logger.info("Beginning ticket cleanup...");
            final Collection<Ticket> ticketsToRemove = Collections2.filter(this.getTickets(), new Predicate<Ticket>() {
                @Override
                public boolean apply(@Nullable final Ticket ticket) {
                    if (ticket.isExpired()) {
                        if (ticket instanceof TicketGrantingTicket) {
                            logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                            logoutManager.performLogout((TicketGrantingTicket) ticket);
                            deleteTicket(ticket.getId());
                        } else if (ticket instanceof ServiceTicket) {
                            logger.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                            deleteTicket(ticket.getId());
                        } else {
                            logger.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                        }
                        return true;
                    }
                    return false;
                }
            });
            logger.info("{} expired tickets found and removed.", ticketsToRemove.size());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            this.jpaLockingStrategy.release();
            logger.info("Finished ticket cleanup.");
        }

    }

    private boolean shouldScheduleCleanerJob() {
        if (this.startDelay > 0 && this.applicationContext.getParent() == null && scheduler != null) {
            logger.debug("Found CAS servlet application context for ticket management");
            return true;
        }

        return false;
    }
}
