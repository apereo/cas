package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.events.CasRegisteredServiceDeletedEvent;
import org.jasig.cas.support.events.CasRegisteredServiceSavedEvent;
import org.jasig.inspektr.audit.annotation.Audit;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link ServicesManager} interface. If there are
 * no services registered with the server, it considers the ServicecsManager
 * disabled and will not prevent any service from using CAS.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("servicesManager")
public final class DefaultServicesManagerImpl implements ReloadableServicesManager, ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServicesManagerImpl.class);

    /**
     * Instance of ServiceRegistryDao.
     */
    @NotNull
    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    /** Application event publisher. */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Map to store all services.
     */
    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<>();

    @Value("${service.registry.quartz.reloader.repeatInterval:60}")
    private int refreshInterval;

    @Value("${service.registry.quartz.reloader.startDelay:15}")
    private int startDelay;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("scheduler")
    private Scheduler scheduler;

    /**
     * Instantiates a new default services manager impl.
     */
    public DefaultServicesManagerImpl() {
    }

    /**
     * Instantiates a new default services manager impl.
     *
     * @param serviceRegistryDao the service registry dao
     */

    @Autowired
    public DefaultServicesManagerImpl(@Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;

        load();
    }

    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService r = findServiceBy(id);
        if (r == null) {
            return null;
        }

        this.serviceRegistryDao.delete(r);
        this.services.remove(id);

        this.eventPublisher.publishEvent(new CasRegisteredServiceDeletedEvent(this, r));
        return r;
    }


    @Override
    public RegisteredService findServiceBy(final Service service) {
        final Collection<RegisteredService> c = convertToTreeSet();

        for (final RegisteredService r : c) {
            if (r.matches(service)) {
                return r;
            }
        }

        return null;
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);

        try {
            return r == null ? null : r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }

    /**
     * Stuff services to tree set.
     *
     * @return the tree set
     */
    public TreeSet<RegisteredService> convertToTreeSet() {
        return new TreeSet<>(this.services.values());
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(convertToTreeSet());
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        this.eventPublisher.publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        return r;
    }

    @Override
    public void reload() {
        LOGGER.info("Reloading registered services.");
        load();
    }

    /**
     * Load services that are provided by the DAO.
     */
    public void load() {
        final ConcurrentHashMap<Long, RegisteredService> localServices =
            new ConcurrentHashMap<>();

        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            LOGGER.debug("Adding registered service {}", r.getServiceId());
            localServices.put(r.getId(), r);
        }

        this.services = localServices;
        LOGGER.info("Loaded {} services from {}.", this.services.size(),
            this.serviceRegistryDao);

    }

    /**
     * Schedule reloader job.
     */
    @PostConstruct
    public void scheduleReloaderJob() {
        try {
            if (shouldScheduleLoaderJob()) {
                LOGGER.debug("Preparing to schedule reloader job");

                final JobDetail job = JobBuilder.newJob(ServiceRegistryReloaderJob.class)
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .build();

                final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(this.getClass().getSimpleName().concat(UUID.randomUUID().toString()))
                    .startAt(DateTime.now().plusSeconds(this.startDelay).toDate())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(this.refreshInterval)
                        .repeatForever()).build();

                LOGGER.debug("Scheduling {} job", this.getClass().getName());
                scheduler.scheduleJob(job, trigger);
                LOGGER.info("Services manager will reload service definitions every {} seconds",
                    this.refreshInterval);
            }

        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private boolean shouldScheduleLoaderJob() {
        if (this.startDelay > 0 && this.applicationContext.getParent() == null && scheduler != null) {
            LOGGER.debug("Found CAS servlet application context for service management");
            return true;
        }

        return false;
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    /**
     * The Service registry reloader job.
     */
    public static class ServiceRegistryReloaderJob implements Job {

        @Autowired
        @Qualifier("servicesManager")
        private ReloadableServicesManager servicesManager;

        @Override
        public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
            try {
                servicesManager.reload();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }
}
