package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link AbstractServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractServicesManager implements ServicesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServicesManager.class);

    private static final long serialVersionUID = -8581398063126547772L;

    private final ServiceRegistryDao serviceRegistryDao;

    private final transient ApplicationEventPublisher eventPublisher;

    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();

    public AbstractServicesManager(final ServiceRegistryDao serviceRegistryDao,
                                   final ApplicationEventPublisher eventPublisher) {
        this.serviceRegistryDao = serviceRegistryDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return this.services.values()
                .stream()
                .filter(getRegisteredServicesFilteringPredicate())
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> predicate) {
        if (predicate == null) {
            return new ArrayList<>(0);
        }
        
        return getAllServices()
                .stream()
                .filter(getRegisteredServicesFilteringPredicate(predicate))
                .sorted()
                .collect(Collectors.toSet());
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }
        
        final RegisteredService service = getCandidateServicesToMatch(serviceId)
                .stream()
                .filter(r -> r.matches(serviceId))
                .findFirst()
                .orElse(null);
        final RegisteredService result = validateRegisteredService(service);
        return result;
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        return service != null ? findServiceBy(service.getId()) : null;
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final String serviceId, final Class<T> clazz) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }
        final RegisteredService service = findServiceBy(serviceId);
        if (service != null && service.getClass().isAssignableFrom(clazz)) {
            return (T) service;
        }
        return null;
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final Service serviceId, final Class<T> clazz) {
        return findServiceBy(serviceId.getId(), clazz);
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);
        return r == null ? null : r.clone();
    }

    @Override
    public int count() {
        return services.size();
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return matchesExistingService(service.getId());
    }

    @Override
    public boolean matchesExistingService(final String service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "DELETE_SERVICE",
            actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService service = findServiceBy(id);
        return delete(service);
    }

    @Audit(action = "DELETE_SERVICE",
            actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final RegisteredService service) {
        if (service != null) {
            publishEvent(new CasRegisteredServicePreDeleteEvent(this, service));
            this.serviceRegistryDao.delete(service);
            this.services.remove(service.getId());
            deleteInternal(service);
            publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        }
        return service;
    }

    @Audit(action = "SAVE_SERVICE",
            actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return save(registeredService, true);
    }

    @Audit(action = "SAVE_SERVICE",
            actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService));
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        saveInternal(registeredService);

        if (publishEvent) {
            publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        }
        return r;
    }

    /**
     * Load services that are provided by the DAO.
     */
    @Scheduled(initialDelayString = "${cas.serviceRegistry.schedule.startDelay:20000}",
            fixedDelayString = "${cas.serviceRegistry.schedule.repeatInterval:60000}")
    @Override
    @PostConstruct
    public void load() {
        LOGGER.debug("Loading services from [{}]", this.serviceRegistryDao);
        this.services = this.serviceRegistryDao.load()
                .stream()
                .collect(Collectors.toConcurrentMap(r -> {
                    LOGGER.debug("Adding registered service [{}]", r.getServiceId());
                    return r.getId();
                }, Function.identity(), (r, s) -> s == null ? r : s));
        loadInternal();
        publishEvent(new CasRegisteredServicesLoadedEvent(this, getAllServices()));
        evaluateExpiredServiceDefinitions();
        LOGGER.info("Loaded [{}] service(s) from [{}].", this.services.size(), this.serviceRegistryDao);
    }

    private void evaluateExpiredServiceDefinitions() {
        this.services.values()
                .stream()
                .filter(getRegisteredServicesFilteringPredicate().negate())
                .filter(Objects::nonNull)
                .forEach(this::processExpiredRegisteredService);
    }

    private Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(final Predicate<RegisteredService>... p) {
        final List<Predicate<RegisteredService>> predicates = new ArrayList<>();

        final Predicate<RegisteredService> expirationPolicyPredicate = getRegisteredServiceExpirationPolicyPredicate();
        predicates.add(expirationPolicyPredicate);

        predicates.addAll(Stream.of(p).collect(Collectors.toList()));
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    /**
     * Returns a predicate that determined whether a service has expired.
     *
     * @return true if the service is still valid. false if service has expired.
     */
    private Predicate<RegisteredService> getRegisteredServiceExpirationPolicyPredicate() {
        return service -> {
            try {
                if (service == null) {
                    return false;
                }
                final RegisteredServiceExpirationPolicy policy = service.getExpirationPolicy();
                if (policy == null || StringUtils.isBlank(policy.getExpirationDate())) {
                    return true;
                }
                final LocalDateTime now = getCurrentSystemTime();
                final LocalDateTime expirationDate = DateTimeUtils.localDateTimeOf(policy.getExpirationDate());
                LOGGER.debug("Service expiration date is [{}] while now is [{}]", expirationDate, now);
                return !now.isAfter(expirationDate);
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
            return false;
        };
    }

    /**
     * Gets current system time.
     *
     * @return the current system time
     */
    protected LocalDateTime getCurrentSystemTime() {
        return LocalDateTime.now();
    }

    private RegisteredService validateRegisteredService(final RegisteredService registeredService) {
        final RegisteredService result = checkServiceExpirationPolicyIfAny(registeredService);
        return result;
    }

    private RegisteredService checkServiceExpirationPolicyIfAny(final RegisteredService registeredService) {
        if (registeredService == null || getRegisteredServiceExpirationPolicyPredicate().test(registeredService)) {
            return registeredService;
        }
        return processExpiredRegisteredService(registeredService);
    }

    private RegisteredService processExpiredRegisteredService(final RegisteredService registeredService) {
        final RegisteredServiceExpirationPolicy policy = registeredService.getExpirationPolicy();
        LOGGER.warn("Registered service [{}] has expired on [{}]", registeredService.getServiceId(), policy.getExpirationDate());

        if (policy.isDeleteWhenExpired()) {
            LOGGER.debug("Deleting expired registered service [{}] from registry.", registeredService.getServiceId());
            if (policy.isNotifyWhenDeleted()) {
                LOGGER.debug("Contacts for registered service [{}] will be notified of service expiry", registeredService.getServiceId());
                publishEvent(new CasRegisteredServiceExpiredEvent(this, registeredService));
            }
            delete(registeredService);
            return null;
        }
        LOGGER.debug("Disabling expired registered service [{}].", registeredService.getServiceId());
        registeredService.getAccessStrategy().setServiceAccessAllowed(false);
        return save(registeredService);
    }

    /**
     * Gets candidate services to match the service id.
     *
     * @param serviceId the service id
     * @return the candidate services to match
     */
    protected abstract Collection<RegisteredService> getCandidateServicesToMatch(String serviceId);

    /**
     * Delete internal.
     *
     * @param service the service
     */
    protected void deleteInternal(final RegisteredService service) {
    }

    /**
     * Save internal.
     *
     * @param service the service
     */
    protected void saveInternal(final RegisteredService service) {
    }

    /**
     * Load internal.
     */
    protected void loadInternal() {
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
