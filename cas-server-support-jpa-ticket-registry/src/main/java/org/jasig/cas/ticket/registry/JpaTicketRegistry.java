package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.joda.time.DateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.2.1
 */
@Component("jpaTicketRegistry")
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "ticketTransactionManager")
public class JpaTicketRegistry extends AbstractDistributedTicketRegistry {
    private static final String TABLE_SERVICE_TICKETS = ServiceTicketImpl.class.getSimpleName();
    private static final String TABLE_TICKET_GRANTING_TICKETS = TicketGrantingTicketImpl.class.getSimpleName();

    @Value("${ticket.registry.cleaner.repeatinterval:300}")
    private int refreshInterval;

    @Value("${ticket.registry.cleaner.startdelay:20}")
    private int startDelay;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;
    
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
     * @return true if ticket was removed
     */
    public boolean removeTicket(final Ticket ticket) {
        try {
            if (logger.isDebugEnabled()) {
                final Date creationDate = new Date(ticket.getCreationTime());
                logger.debug("Removing Ticket [{}] created: {}", ticket, creationDate.toString());
             }
            entityManager.remove(ticket);
            return true;
        } catch (final Exception e) {
            logger.error("Error removing {} from registry.", ticket, e);
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
    public Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                    || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
                // There is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
                return entityManager.find(TicketGrantingTicketImpl.class, ticketId);
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
        if (ticket == null) {
            return true;
        }

        final int failureCount;

        if (ticket instanceof ServiceTicket) {
            failureCount = deleteServiceTickets(ticketId);
        } else if (ticket instanceof TicketGrantingTicket) {
            failureCount = deleteTicketGrantingTickets(ticketId);
        } else {
            throw new IllegalArgumentException("Invalid ticket type with id " + ticketId);
        }
        return failureCount == 0;
    }

    /**
     * Gets ticket query result list.
     *
     * @param <T>      the type parameter
     * @param ticketId the ticket id
     * @param query    the query
     * @param clazz    the clazz
     * @return the ticket query result list
     */
    public <T extends Ticket> List<T> getTicketQueryResultList(final String ticketId, final String query,
                                                               final Class<T> clazz) {
        return this.entityManager.createQuery(query, clazz)
                .setParameter("id", ticketId)
                .getResultList();
    }

    /**
     * Delete service tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    public int deleteServiceTickets(final String ticketId) {
        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from " + TABLE_SERVICE_TICKETS + " s where s.id = :id", ServiceTicketImpl.class);
        return deleteTicketsFromResultList(serviceTicketImpls);
    }

    /**
     * Delete tickets from result list int.
     *
     * @param serviceTicketImpls the service ticket impls
     * @return the int
     */
    public int deleteTicketsFromResultList(final List<? extends Ticket> serviceTicketImpls) {
        int failureCount = 0;
        for (final Ticket serviceTicketImpl : serviceTicketImpls) {
            if (!removeTicket(serviceTicketImpl)) {
                failureCount++;
            }
        }
        return failureCount;
    }

    /**
     * Delete ticket granting tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    public int deleteTicketGrantingTickets(final String ticketId) {
        int failureCount = 0;

        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from "
                + TABLE_SERVICE_TICKETS
                + " s where s.ticketGrantingTicket.id = :id", ServiceTicketImpl.class);
        failureCount += deleteTicketsFromResultList(serviceTicketImpls);

        List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from " + TABLE_TICKET_GRANTING_TICKETS
                + " t where t.ticketGrantingTicket.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from " + TABLE_TICKET_GRANTING_TICKETS
                + " t where t.id = :id", TicketGrantingTicketImpl.class);
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

                final JobDetail job = JobBuilder.newJob(JpaTicketRegistryCleaner.class)
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .build();

                final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .startAt(DateTime.now().plusSeconds(this.startDelay).toDate())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(this.refreshInterval)
                        .repeatForever()).build();

                logger.debug("Scheduling {} job", this.getClass().getName());
                scheduler.getContext().put(getClass().getSimpleName(), this);
                scheduler.scheduleJob(job, trigger);
                logger.info("{} will clean tickets every {} seconds",
                    this.getClass().getSimpleName(),
                    this.refreshInterval);
            }
        } catch (final Exception e){
            logger.warn(e.getMessage(), e);
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
