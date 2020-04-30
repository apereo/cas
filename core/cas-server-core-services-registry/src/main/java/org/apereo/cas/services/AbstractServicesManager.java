package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
@RequiredArgsConstructor
@Getter
public abstract class AbstractServicesManager implements ServicesManager {

    private final ServiceRegistry serviceRegistry;

    private final transient ApplicationEventPublisher eventPublisher;

    private final Set<String> environments;

    @Getter(value = AccessLevel.PROTECTED)
    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();

    private static Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(final Predicate<RegisteredService>... p) {
        val predicates = Stream.of(p).collect(Collectors.toCollection(ArrayList::new));
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return save(registeredService, true);
    }

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
    public synchronized void deleteAll() {
        this.services.forEach((k, v) -> delete(v));
        this.services.clear();
        publishEvent(new CasRegisteredServicesDeletedEvent(this));
    }

    @Override
    public synchronized RegisteredService delete(final long id) {
        val service = findServiceBy(id);
        return delete(service);
    }

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

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }

        val service = getCandidateServicesToMatch(serviceId)
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
        return Optional.ofNullable(service)
            .map(svc -> findServiceBy(svc.getId()))
            .orElse(null);
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
    public <T extends RegisteredService> T findServiceBy(final Service serviceId, final Class<T> clazz) {
        return findServiceBy(serviceId.getId(), clazz);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final String serviceId, final Class<T> clazz) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }
        val service = findServiceBy(serviceId);
        if (service != null && service.getClass().equals(clazz)) {
            return (T) service;
        }
        return null;
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        val result = this.services.get(id);
        return validateRegisteredService(result);
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return this.services.values()
            .stream()
            .filter(this::validateAndFilterServiceByEnvironment)
            .filter(getRegisteredServicesFilteringPredicate())
            .sorted()
            .peek(RegisteredService::initialize)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> load() {
        LOGGER.trace("Loading services from [{}]", serviceRegistry.getName());
        this.services = this.serviceRegistry.load()
            .stream()
            .collect(Collectors.toConcurrentMap(r -> {
                LOGGER.debug("Adding registered service [{}] with name [{}] and internal identifier [{}]", r.getServiceId(), r.getName(), r.getId());
                return r.getId();
            }, Function.identity(), (r, s) -> s));
        loadInternal();
        publishEvent(new CasRegisteredServicesLoadedEvent(this, getAllServices()));
        evaluateExpiredServiceDefinitions();
        LOGGER.info("Loaded [{}] service(s) from [{}].", this.services.size(), this.serviceRegistry.getName());
        return services.values();
    }

    @Override
    public int count() {
        return services.size();
    }

    private void evaluateExpiredServiceDefinitions() {
        this.services.values()
            .stream()
            .filter(RegisteredServiceAccessStrategyUtils.getRegisteredServiceExpirationPolicyPredicate().negate())
            .filter(Objects::nonNull)
            .forEach(this::processExpiredRegisteredService);
    }

    private RegisteredService validateRegisteredService(final RegisteredService registeredService) {
        val result = checkServiceExpirationPolicyIfAny(registeredService);
        if (validateAndFilterServiceByEnvironment(result)) {
            return result;
        }
        return null;
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

        if (policy.isNotifyWhenExpired()) {
            LOGGER.debug("Contacts for registered service [{}] will be notified of service expiry", registeredService.getServiceId());
            publishEvent(new CasRegisteredServiceExpiredEvent(this, registeredService, false));
        }
        if (policy.isDeleteWhenExpired()) {
            LOGGER.debug("Deleting expired registered service [{}] from registry.", registeredService.getServiceId());
            if (policy.isNotifyWhenDeleted()) {
                LOGGER.debug("Contacts for registered service [{}] will be notified of service expiry and removal", registeredService.getServiceId());
                publishEvent(new CasRegisteredServiceExpiredEvent(this, registeredService, true));
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
    protected abstract Stream<RegisteredService> getCandidateServicesToMatch(String serviceId);

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

    private boolean validateAndFilterServiceByEnvironment(final RegisteredService service) {
        if (this.environments.isEmpty()) {
            LOGGER.trace("No environments are defined by which services could be filtered");
            return true;
        }
        if (service == null) {
            LOGGER.trace("No service definition was provided");
            return true;
        }
        if (service.getEnvironments() == null || service.getEnvironments().isEmpty()) {
            LOGGER.trace("No environments are assigned to service [{}]", service.getName());
            return true;
        }
        return service.getEnvironments()
            .stream()
            .anyMatch(this.environments::contains);
    }
}
