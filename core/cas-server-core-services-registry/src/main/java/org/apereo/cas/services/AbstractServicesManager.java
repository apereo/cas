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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractServicesManager implements ServicesManager {

    /**
     * The Configuration context.
     */
    protected final ServicesManagerConfigurationContext configurationContext;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return save(registeredService, true);
    }

    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService,
                                               final boolean publishEvent) {
        publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService));
        val r = configurationContext.getServiceRegistry().save(registeredService);
        configurationContext.getServicesCache().put(r.getId(), r);
        saveInternal(registeredService);

        if (publishEvent) {
            publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        }
        return r;
    }

    @Override
    public synchronized void deleteAll() {
        configurationContext.getServicesCache().asMap().forEach((k, v) -> delete(v));
        configurationContext.getServicesCache().invalidateAll();
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
            configurationContext.getServiceRegistry().delete(service);
            configurationContext.getServicesCache().invalidate(service.getId());
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

        var service = configurationContext.getRegisteredServiceLocators()
            .stream()
            .map(locator -> locator.locate(getCandidateServicesToMatch(serviceId), serviceId, entry -> entry.matches(serviceId)))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        if (service == null) {
            val serviceRegistry = configurationContext.getServiceRegistry();
            LOGGER.trace("Service [{}] is not cached; Searching [{}]", serviceId, serviceRegistry.getName());
            service = serviceRegistry.findServiceBy(serviceId);
            if (service != null) {
                configurationContext.getServicesCache().put(service.getId(), service);
                LOGGER.trace("Service [{}] is found in [{}] and cached", service, serviceRegistry.getName());
            }
        }

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
        val results = configurationContext.getServiceRegistry().findServicePredicate(predicate).
            stream().
            sorted().
            peek(RegisteredService::initialize).
            collect(Collectors.toMap(RegisteredService::getId, Function.identity(), (r, s) -> s));
        configurationContext.getServicesCache().putAll(results);
        return results.values();
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
        val result = configurationContext.getServicesCache().get(id, k -> configurationContext.getServiceRegistry().findServiceById(id));
        return validateRegisteredService(result);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final long id, final Class<T> clazz) {
        var service = getService(registeredService -> registeredService.getId() == id);
        if (service != null && service.getClass().equals(clazz)) {
            return (T) service;
        }
        LOGGER.trace("The service with id [{}] and type [{}] is not found in the cache; trying to find it from [{}]",
            id, clazz, configurationContext.getServiceRegistry().getName());
        service = configurationContext.getServicesCache().get(id, k -> configurationContext.getServiceRegistry().findServiceById(id, clazz));
        return (T) validateRegisteredService(service);
    }

    @Override
    public RegisteredService findServiceByName(final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        var service = getService(registeredService -> registeredService.getName().equals(name));
        if (service == null) {
            LOGGER.trace("The service with name [{}] is not found in the cache; trying to find it from [{}]",
                name, configurationContext.getServiceRegistry().getName());
            service = configurationContext.getServiceRegistry().findServiceByExactServiceName(name);
            if (service != null) {
                configurationContext.getServicesCache().put(service.getId(), service);
                LOGGER.trace("The service is found in [{}] and populated to the cache [{}]",
                    configurationContext.getServiceRegistry().getName(), service);
            }
        }

        if (service != null) {
            service.initialize();
        }
        return validateRegisteredService(service);
    }

    @Override
    public <T extends RegisteredService> T findServiceByName(final String name, final Class<T> clazz) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        var service = getService(registeredService -> registeredService.getName().equals(name));
        if (service != null && service.getClass().equals(clazz)) {
            return (T) service;
        }
        LOGGER.trace("The service with name [{}] and type [{}] is not found in the cache; trying to find it from [{}]",
            name, clazz, configurationContext.getServiceRegistry().getName());
        service = configurationContext.getServiceRegistry().findServiceByExactServiceName(name, clazz);
        if (service != null) {
            configurationContext.getServicesCache().put(service.getId(), service);
            LOGGER.trace("The service is found in [{}] and populated to the cache [{}]", configurationContext.getServiceRegistry().getName(), service);
        }
        return (T) validateRegisteredService(service);
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }

        var service = configurationContext.getRegisteredServiceLocators()
            .stream()
            .map(locator -> locator.locate(getCandidateServicesToMatch(serviceId), serviceId, entry -> entry.getServiceId().equals(serviceId)))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        
        if (service == null) {
            LOGGER.trace("The service with service id [{}] is not found in the cache; trying to find it from [{}]",
                serviceId, configurationContext.getServiceRegistry().getName());
            service = configurationContext.getServiceRegistry().findServiceByExactServiceId(serviceId);
            if (service != null) {
                configurationContext.getServicesCache().put(service.getId(), service);
                LOGGER.trace("The service is found in [{}] and populated to the cache [{}]  ", configurationContext.getServiceRegistry().getName(),
                    service);
            }
        }

        if (service != null) {
            service.initialize();
        }
        return validateRegisteredService(service);
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return configurationContext.getServicesCache().asMap().values()
            .stream()
            .filter(this::validateAndFilterServiceByEnvironment)
            .filter(getRegisteredServicesFilteringPredicate())
            .sorted()
            .peek(RegisteredService::initialize)
            .collect(Collectors.toList());
    }

    @Override
    public Stream<? extends RegisteredService> getAllServicesStream() {
        return configurationContext.getServiceRegistry().getServicesStream();
    }

    @Override
    public Collection<RegisteredService> load() {
        LOGGER.trace("Loading services from [{}]", configurationContext.getServiceRegistry().getName());
        configurationContext.getServicesCache().invalidateAll();
        configurationContext.getServicesCache().putAll(configurationContext.getServiceRegistry().load()
            .stream()
            .collect(Collectors.toMap(r -> {
                LOGGER.trace("Adding registered service [{}] with name [{}] and internal identifier [{}]",
                    r.getServiceId(), r.getName(), r.getId());
                return r.getId();
            }, Function.identity(), (r, s) -> s)));
        loadInternal();
        publishEvent(new CasRegisteredServicesLoadedEvent(this, getAllServices()));
        evaluateExpiredServiceDefinitions();
        LOGGER.info("Loaded [{}] service(s) from [{}].", configurationContext.getServicesCache().asMap().size(), configurationContext.getServiceRegistry().getName());
        return configurationContext.getServicesCache().asMap().values();
    }

    @Override
    public long count() {
        return configurationContext.getServiceRegistry().size();
    }

    private static Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(
        final Predicate<RegisteredService>... p) {
        val predicates = Stream.of(p).collect(Collectors.toCollection(ArrayList::new));
        return predicates.stream().reduce(x -> true, Predicate::and);
    }

    private void evaluateExpiredServiceDefinitions() {
        configurationContext.getServicesCache().asMap().values()
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
        if (registeredService == null || RegisteredServiceAccessStrategyUtils.ensureServiceIsNotExpired(
            registeredService)) {
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

    private void publishEvent(final ApplicationEvent event) {
        if (configurationContext.getApplicationContext() != null) {
            configurationContext.getApplicationContext().publishEvent(event);
        }
    }

    private boolean validateAndFilterServiceByEnvironment(final RegisteredService service) {
        if (configurationContext.getEnvironments().isEmpty()) {
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
            .anyMatch(configurationContext.getEnvironments()::contains);
    }

    private RegisteredService getService(final Predicate<RegisteredService> filter) {
        return configurationContext.getServicesCache().asMap()
            .values()
            .stream()
            .filter(filter)
            .findFirst()
            .orElse(null);
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
}
