package org.jasig.cas.ticket.registry;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.joda.time.DateTime;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.cas.logout.LogoutManager;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * This is {@link TicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 4.2.5
 */
@Component("ticketRegistryCleaner")
public class TicketRegistryCleaner implements Job {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;
    
    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;

    @Value("${ticket.registry.cleaner.repeatinterval:120}")
    private int refreshInterval;

    @Value("${ticket.registry.cleaner.startdelay:20}")
    private int startDelay;

    @Autowired
    @NotNull
    private ApplicationContext applicationContext;

    /**
     * Instantiates a new Ticket registry cleaner.
     */
    public TicketRegistryCleaner() { }

    /**
     * Schedule reloader job.
     */
    @PostConstruct
    public void scheduleCleanerJob() {
        try {
            if (shouldScheduleCleanerJob()) {
                logger.info("Preparing to schedule job to clean up after tickets...");

                final JobDetail job = JobBuilder.newJob(getClass())
                        .withIdentity(getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                        .build();

                final Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                        .startAt(DateTime.now().plusSeconds(this.startDelay).toDate())
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(this.refreshInterval)
                                .repeatForever()).build();

                logger.debug("Scheduling {} job", getClass().getSimpleName());
                scheduler.getContext().put(getClass().getSimpleName(),
                        this.applicationContext.getBean("ticketRegistry", TicketRegistry.class));
                scheduler.scheduleJob(job, trigger);
                logger.info("{} will clean tickets every {} minutes",
                        this.getClass().getSimpleName(),
                        TimeUnit.SECONDS.toMinutes(this.refreshInterval));
            } else {
                logger.info("Ticket registry cleaner job will not be scheduled to run.");
            }
        } catch (final Exception e){
            logger.warn(e.getMessage(), e);
        }

    }

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            logger.info("Beginning ticket cleanup...");
            final TicketRegistry registry = (TicketRegistry)
                    jobExecutionContext.getScheduler().getContext().get(getClass().getSimpleName());
            logger.debug("Cleaning up tickets from an instance of {}", registry);

            final Collection<Integer> deletedTicketCounts = Collections2.transform(registry.getTickets(), new Function<Ticket, Integer>() {
                @Override
                public Integer apply(@Nullable final Ticket ticket) {
                    int count = 0;

                    if (ticket != null && ticket.isExpired()) {
                        if (ticket instanceof TicketGrantingTicket) {
                            logger.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.getId());
                            logoutManager.performLogout((TicketGrantingTicket) ticket);
                            count += registry.deleteTicket(ticket.getId());
                        } else if (ticket instanceof ServiceTicket) {
                            logger.debug("Cleaning up expired service ticket [{}]", ticket.getId());
                            count += registry.deleteTicket(ticket.getId());
                        } else {
                            logger.warn("Unknown ticket type [{} found to clean", ticket.getClass().getSimpleName());
                        }
                    }
                    return count;
                }
            });
            
            int cumulativeCount = 0;
            for (final int count : deletedTicketCounts) {
                cumulativeCount += count;
            }
            
            logger.info("{} expired tickets found and removed.", cumulativeCount);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean shouldScheduleCleanerJob() {
        if (this.startDelay > 0 && this.applicationContext.getParent() == null && scheduler != null) {
            if (WebUtils.isCasServletInitializing(this.applicationContext)) {
                return true;
            } else {
                logger.debug("Could not find CAS servlet application context");
            }
        }
        return false;
    }
}
