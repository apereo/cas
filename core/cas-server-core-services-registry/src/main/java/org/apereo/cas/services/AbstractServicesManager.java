package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
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
@Slf4j
public abstract class AbstractServicesManager implements ServicesManager, InitializingBean {

    private static final long serialVersionUID = -8581398063126547772L;

    private final ServiceRegistry serviceRegistry;

    private final transient ApplicationEventPublisher eventPublisher;

    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();

    public AbstractServicesManager(final ServiceRegistry serviceRegistry,
                                   final ApplicationEventPublisher eventPublisher) {
        this.serviceRegistry = serviceRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return this.services.values()
            .stream()
            .filter(getRegisteredServicesFilteringPredicate())
            .sorted()
            .peek(RegisteredService::initialize)
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
            .peek(RegisteredService::initialize)
            .collect(Collectors.toList());

    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }

        val service = getCandidateServicesToMatch(serviceId)
            .stream()
            .filter(r -> r.matches(serviceId))
            .findFirst()
            .orElse(null);

        if (service != null) {
            service.initialize();
        }
        return validateRegisteredService(service);
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
        val service = findServiceBy(serviceId);
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
        return this.services.get(id);
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
        val service = findServiceBy(id);
        return delete(service);
    }

    @Audit(action = "DELETE_SERVICE",
        actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
        resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final RegisteredService service) {
        if (service != null) {
            publishEvent(new CasRegisteredServicePreDeleteEvent(this, service));
            this.serviceRegistry.delete(service);
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
        val r = this.serviceRegistry.save(registeredService);
        this.services.put(r.getId(), r);
        saveInternal(registeredService);

        if (publishEvent) {
            publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        }
        return r;
    }

    @Override
    public void afterPropertiesSet() {
        load();
    }

    /**
     * Load services that are provided by the DAO.
     */
    @Scheduled(initialDelayString = "${cas.serviceRegistry.schedule.startDelay:20000}",
        fixedDelayString = "${cas.serviceRegistry.schedule.repeatInterval:60000}")
    @Override
    public Collection<RegisteredService> load() {
        LOGGER.debug("Loading services from [{}]", this.serviceRegistry);
        this.services = this.serviceRegistry.load()
            .stream()
            .collect(Collectors.toConcurrentMap(r -> {
                LOGGER.debug("Adding registered service [{}]", r.getServiceId());
                return r.getId();
            }, Function.identity(), (r, s) -> s == null ? r : s));
        loadInternal();
        publishEvent(new CasRegisteredServicesLoadedEvent(this, getAllServices()));
        evaluateExpiredServiceDefinitions();
        LOGGER.info("Loaded [{}] service(s) from [{}].", this.services.size(), this.serviceRegistry.getName());
        return services.values();
    }

    @Override
    public synchronized void deleteAll() {
        this.services.forEach((k, v) -> delete(v));
        this.services.clear();
        publishEvent(new CasRegisteredServicesDeletedEvent(this));
    }

    private void evaluateExpiredServiceDefinitions() {
        this.services.values()
            .stream()
            .filter((Predicate<RegisteredService>) getRegisteredServicesFilteringPredicate().negate())
            .filter(Objects::nonNull)
            .forEach(this::processExpiredRegisteredService);
    }

    private Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(final Predicate<RegisteredService>... p) {
        val predicates = new ArrayList<Predicate<RegisteredService>>();

        predicates.addAll(Stream.of(p).collect(Collectors.toList()));
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    private RegisteredService validateRegisteredService(final RegisteredService registeredService) {
        val result = checkServiceExpirationPolicyIfAny(registeredService);
        return result;
    }

    private RegisteredService checkServiceExpirationPolicyIfAny(final RegisteredService registeredService) {
        if (registeredService == null || RegisteredServiceAccessStrategyUtils.ensureServiceIsNotExpired(registeredService)) {
            return registeredService;
        }
        return processExpiredRegisteredService(registeredService);
    }

    private RegisteredService processExpiredRegisteredService(final RegisteredService registeredService) {
        val policy = registeredService.getExpirationPolicy();
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
        return registeredService;
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
