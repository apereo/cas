package org.jasig.cas.ticket.registry;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CipherExecutor;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.util.DateTimeUtils;
import org.jasig.cas.util.DigestUtils;
import org.jasig.cas.util.SerializationUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry, TicketRegistryState, Job {

    private static final String MESSAGE = "Ticket encryption is not enabled. Falling back to default behavior";

    /**
     * The Slf4j logger instance.
     */
    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ticket.registry.cleaner.enabled:true}")
    private boolean cleanerEnabled;

    @Value("${ticket.registry.cleaner.repeatinterval:120}")
    private int refreshInterval;

    @Value("${ticket.registry.cleaner.startdelay:20}")
    private int startDelay;

    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
    public AbstractTicketRegistry() {
    }

    /**
     * {@inheritDoc}
     *
     * @return specified ticket from the registry
     * @throws IllegalArgumentException if class is null.
     * @throws ClassCastException       if class does not match requested ticket
     *                                  class.
     */
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<T> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");

        final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                    + " is of type " + ticket.getClass()
                    + " when we were expecting " + clazz);
        }

        return (T) ticket;
    }

    @Override
    public long sessionCount() {
        logger.trace("sessionCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Long.MIN_VALUE);
        return Long.MIN_VALUE;
    }

    @Override
    public long serviceTicketCount() {
        logger.trace("serviceTicketCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Long.MIN_VALUE);
        return Long.MIN_VALUE;
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        if (ticketId == null) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            if (ticket instanceof ProxyGrantingTicket) {
                logger.debug("Removing proxy-granting ticket [{}]", ticketId);
            }

            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            deleteChildren(tgt);

            final Collection<ProxyGrantingTicket> proxyGrantingTickets = tgt.getProxyGrantingTickets();
            proxyGrantingTickets.stream().map(Ticket::getId).forEach(this::deleteTicket);
        }
        logger.debug("Removing ticket [{}] from the registry.", ticket);
        return deleteSingleTicket(ticketId);
    }


    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     */
    private void deleteChildren(final TicketGrantingTicket ticket) {
        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            services.keySet().stream().forEach(ticketId -> {
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", ticketId);
                } else {
                    logger.debug("Unable to remove ticket [{}]", ticketId);
                }
            });
        }
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public boolean deleteSingleTicket(final Ticket ticketId) {
        return deleteSingleTicket(ticketId.getId());
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public abstract boolean deleteSingleTicket(final String ticketId);

    /**
     * Whether or not a callback to the TGT is required when checking for expiration.
     *
     * @return true, if successful
     */
    protected abstract boolean needsCallback();

    public void setCipherExecutor(final CipherExecutor<byte[], byte[]> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encode ticket id into a SHA-512.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    protected String encodeTicketId(final String ticketId) {
        if (this.cipherExecutor == null) {
            logger.trace(MESSAGE);
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }

        return DigestUtils.sha512(ticketId);
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket) {
        if (this.cipherExecutor == null) {
            logger.trace(MESSAGE);
            return ticket;
        }

        if (ticket == null) {
            logger.debug("Ticket passed is null and cannot be encoded");
            return null;
        }

        logger.info("Encoding [{}]", ticket);
        final byte[] encodedTicketObject = SerializationUtils.serializeAndEncodeObject(
                this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final Ticket encodedTicket = new EncodedTicket(
                ByteSource.wrap(encodedTicketObject),
                encodedTicketId);
        logger.info("Created [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket result) {
        if (this.cipherExecutor == null) {
            logger.trace(MESSAGE);
            return result;
        }

        if (result == null) {
            logger.debug("Ticket passed is null and cannot be decoded");
            return null;
        }

        logger.info("Attempting to decode {}", result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = SerializationUtils.decodeAndSerializeObject(
                encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
        logger.info("Decoded {}", ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        if (this.cipherExecutor == null) {
            logger.trace(MESSAGE);
            return items;
        }

        return items.stream().map(this::decodeTicket).collect(Collectors.toSet());
    }

    /**
     * Common code to go over expired tickets and clean them up.
     **/
    protected void cleanupTickets() {
        try {
            if (preCleanupTickets()) {
                logger.debug("Beginning ticket cleanup...");
                this.getTickets().stream()
                        .filter(Ticket::isExpired)
                        .forEach(ticket -> {
                            if (ticket instanceof TicketGrantingTicket) {
                                logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                                this.logoutManager.performLogout((TicketGrantingTicket) ticket);
                                deleteTicket(ticket.getId());
                            } else if (ticket instanceof ServiceTicket) {
                                logger.debug("Cleaning up expired service ticket or its derivative [{}]", ticket.getId());
                                deleteTicket(ticket.getId());
                            } else {
                                logger.warn("Unknown ticket type [{}]. Nothing to clean up.", ticket.getClass().getSimpleName());
                            }
                        });
            }
        } finally {
            postCleanupTickets();
        }

    }

    /**
     * Post cleanup tickets. This injection point is always executed
     * in a finally block regardless of whether cleanup actually happened.
     */
    protected void postCleanupTickets() {

    }

    /**
     * Pre cleanup tickets.
     *
     * @return true, if cleanup should proceed. false otherwise.
     */
    protected boolean preCleanupTickets() {
        return this.scheduler != null;
    }

    /**
     * Schedule reloader job.
     */
    @PostConstruct
    protected void scheduleCleanerJob() {
        try {

            if (!this.cleanerEnabled) {
                logger.info("Ticket registry cleaner is disabled for {}. No cleaner processes will be scheduled.",
                        getClass().getSimpleName());
                return;
            }

            if (!isCleanerSupported()) {
                logger.info("Ticket registry cleaner is not supported by {}. No cleaner processes will be scheduled.",
                        getClass().getSimpleName());
                return;
            }

            logger.info("Preparing to schedule job to clean up after tickets...");
            final JobDetail job = JobBuilder.newJob(getClass())
                    .withIdentity(getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .build();

            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .startAt(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(this.startDelay)))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(this.refreshInterval)
                            .repeatForever()).build();

            logger.debug("Scheduling {} job", getClass().getSimpleName());
            scheduler.getContext().put(getClass().getSimpleName(), this);
            this.scheduler.scheduleJob(job, trigger);
            logger.info("{} will clean tickets every {} minutes",
                    getClass().getSimpleName(),
                    TimeUnit.SECONDS.toMinutes(this.refreshInterval));
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            final AbstractTicketRegistry registry = (AbstractTicketRegistry)
                    jobExecutionContext.getScheduler().getContext().get(getClass().getSimpleName());
            registry.cleanupTickets();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
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
        return this.scheduler != null;
    }
}
