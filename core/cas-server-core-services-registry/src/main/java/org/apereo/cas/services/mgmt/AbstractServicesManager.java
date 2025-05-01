package org.apereo.cas.services.mgmt;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.IndexableServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.services.query.RegisteredServiceQueryAttribute;
import org.apereo.cas.services.query.RegisteredServiceQueryIndex;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ApplicationEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link AbstractServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public abstract class AbstractServicesManager implements IndexableServicesManager {
    protected final ServicesManagerConfigurationContext configurationContext;

    private final CasReentrantLock lock = new CasReentrantLock();

    private final IndexedCollection<RegisteredService> indexedRegisteredServices;

    protected AbstractServicesManager(final ServicesManagerConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.indexedRegisteredServices = new ConcurrentIndexedCollection<>();
        if (configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()) {
            createRegisteredServiceIndexes();
        }
    }

    private void createRegisteredServiceIndexes() {
        configurationContext.getRegisteredServiceLocators()
            .forEach(locator -> locator.getRegisteredServiceIndexes()
                .stream()
                .map(RegisteredServiceQueryIndex::getIndex)
                .filter(AttributeIndex.class::isInstance)
                .map(AttributeIndex.class::cast)
                .forEach(index -> {
                    LOGGER.debug("Adding registered service index [{}] supplied by [{}]",
                        index.getAttribute().toString(), locator.getClass().getSimpleName());
                    indexedRegisteredServices.addIndex(index);
                }));
    }

    @Override
    public void clearIndexedServices() {
        if (configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()) {
            indexedRegisteredServices.clear();
        }
    }

    @Override
    public long countIndexedServices() {
        return configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()
            ? indexedRegisteredServices.size()
            : 0;
    }

    @Override
    public Optional<RegisteredService> findIndexedServiceBy(final long id) {
        return configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()
            ? indexedRegisteredServices.stream().filter(registeredService -> registeredService.getId() == id).findFirst()
            : Optional.empty();
    }

    @Override
    public void save(final Stream<? extends RegisteredService> toSave) {
        toSave.forEach(this::save);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        return lock.tryLock(() -> {
            val clientInfo = ClientInfoHolder.getClientInfo();
            publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService, clientInfo));
            val savedService = configurationContext.getServiceRegistry().save(registeredService);
            cacheRegisteredService(savedService);
            saveInternal(registeredService);

            if (publishEvent) {
                publishEvent(new CasRegisteredServiceSavedEvent(this, savedService, clientInfo));
            }
            return savedService;
        });
    }

    @Override
    public void save(final Supplier<RegisteredService> supplier,
                     final Consumer<RegisteredService> andThenConsume,
                     final long countExclusive) {
        configurationContext.getServiceRegistry().save(() -> {
            val registeredService = supplier.get();
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (registeredService != null) {
                publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService, clientInfo));
                cacheRegisteredService(registeredService);
                saveInternal(registeredService);
                publishEvent(new CasRegisteredServiceSavedEvent(this, registeredService, clientInfo));
                return registeredService;
            }
            return null;
        }, andThenConsume, countExclusive);
    }

    @Override
    public void deleteAll() {
        lock.tryLock(__ -> {
            configurationContext.getServicesCache().asMap().forEach((key, v) -> delete(v));
            configurationContext.getServicesCache().invalidateAll();
            val clientInfo = ClientInfoHolder.getClientInfo();
            publishEvent(new CasRegisteredServicesDeletedEvent(this, clientInfo));
        });
    }

    @Override
    public RegisteredService delete(final long id) {
        return lock.tryLock(() -> {
            val service = findServiceBy(id);
            return delete(service);
        });
    }

    @Override
    public RegisteredService delete(final RegisteredService service) {
        return lock.tryLock(() -> {
            if (service != null) {
                val clientInfo = ClientInfoHolder.getClientInfo();
                publishEvent(new CasRegisteredServicePreDeleteEvent(this, service, clientInfo));
                configurationContext.getServiceRegistry().delete(service);
                configurationContext.getServicesCache().invalidate(service.getId());
                deleteInternal(service);
                publishEvent(new CasRegisteredServiceDeletedEvent(this, service, clientInfo));
            }
            return service;
        });
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        if (service == null) {
            return null;
        }

        val candidates = getCandidateServicesToMatch(service.getId());
        var foundService = configurationContext.getRegisteredServiceLocators()
            .stream()
            .map(locator -> locator.locate(candidates, service))
            .filter(registeredService -> validateRegisteredService(registeredService) != null)
            .findFirst();
        
        if (foundService.isEmpty()) {
            val serviceRegistry = configurationContext.getServiceRegistry();
            LOGGER.trace("Service [{}] is not cached; Searching [{}]", service.getId(), serviceRegistry.getName());
            foundService = Optional.ofNullable(serviceRegistry.findServiceBy(service.getId()));
            if (foundService.isPresent()) {
                val registeredService = foundService.get();
                foundService = configurationContext.getRegisteredServiceLocators()
                    .stream()
                    .filter(locator -> locator.supports(registeredService, service))
                    .findFirst()
                    .map(locator -> {
                        LOGGER.debug("Service [{}] is found in service registry and can be supported by [{}]", registeredService, locator.getName());
                        cacheRegisteredService(registeredService);
                        LOGGER.trace("Service [{}] is now cached from [{}]", service, serviceRegistry.getName());
                        return Optional.of(registeredService);
                    })
                    .orElseGet(Optional::empty);
            }
        }

        foundService.ifPresent(RegisteredService::initialize);
        return validateRegisteredService(foundService.orElse(null));
    }

    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> predicate) {
        if (predicate == null) {
            return new ArrayList<>();
        }
        val results = configurationContext.getServiceRegistry().findServicePredicate(predicate)
            .stream()
            .sorted()
            .peek(RegisteredService::initialize)
            .collect(Collectors.toMap(RegisteredService::getId, Function.identity(), (r, s) -> s));
        configurationContext.getServicesCache().putAll(results);
        return results.values();
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final Service requestedService, final Class<T> clazz) {
        if (requestedService == null) {
            return null;
        }
        val service = findServiceBy(requestedService);
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) service;
        }
        return null;
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        val result = configurationContext.getServicesCache().get(id,
            __ -> configurationContext.getServiceRegistry().findServiceById(id));
        return validateRegisteredService(result);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final long id, final Class<T> clazz) {
        var service = getService(registeredService -> registeredService.getId() == id);
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) service;
        }
        LOGGER.trace("The service with id [{}] and type [{}] is not found in the cache; trying to find it from [{}]",
            id, clazz, configurationContext.getServiceRegistry().getName());
        service = configurationContext.getServicesCache().get(id,
            __ -> configurationContext.getServiceRegistry().findServiceById(id, clazz));
        return (T) validateRegisteredService(service);
    }

    @Override
    public RegisteredService findServiceByName(final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        var service = getService(registeredService -> registeredService.getName().equals(name));
        if (service == null) {
            val registry = configurationContext.getServiceRegistry();
            LOGGER.trace("The service with name [{}] is not found in the cache; trying to find it from [{}]", name, registry.getName());
            service = registry.findServiceByExactServiceName(name);
            if (service != null) {
                cacheRegisteredService(service);
                LOGGER.trace("The service is found in [{}] and populated to the cache [{}]", registry.getName(), service);
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
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) service;
        }
        LOGGER.trace("The service with name [{}] and type [{}] is not found in the cache; trying to find it from [{}]",
            name, clazz, configurationContext.getServiceRegistry().getName());
        service = configurationContext.getServiceRegistry().findServiceByExactServiceName(name, clazz);
        if (service != null) {
            cacheRegisteredService(service);
            LOGGER.trace("The service is found in [{}] and added to the cache [{}]",
                configurationContext.getServiceRegistry().getName(), service);
        }
        return (T) validateRegisteredService(service);
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return getCacheableServicesStream()
            .get()
            .filter(this::validateAndFilterServiceByEnvironment)
            .filter(getRegisteredServicesFilteringPredicate())
            .sorted()
            .peek(RegisteredService::initialize)
            .peek(this::cacheRegisteredService)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getAllServicesOfType(final Class clazz) {
        if (supports(clazz)) {
            return getCacheableServicesStream()
                .get()
                .filter(service -> clazz.isAssignableFrom(service.getClass()))
                .filter(this::validateAndFilterServiceByEnvironment)
                .filter(getRegisteredServicesFilteringPredicate())
                .sorted()
                .peek(RegisteredService::initialize)
                .peek(this::cacheRegisteredService)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public Stream<? extends RegisteredService> stream() {
        return configurationContext.getServiceRegistry().getServicesStream();
    }

    /**
     * For the duration of the read, the cache store should not remain empty.
     * Otherwise, lookup operations during that loading time window might produce
     * unauthorized failure errors. Invalidation attempts must happen after the load
     * to minimize chances of failures.
     */
    @Override
    public Collection<RegisteredService> load() {
        return lock.tryLock(() -> {
            LOGGER.trace("Loading services from [{}]", configurationContext.getServiceRegistry().getName());
            val servicesMap = configurationContext.getServiceRegistry()
                .load()
                .stream()
                .filter(this::supports)
                .filter(this::validateAndFilterServiceByEnvironment)
                .peek(this::loadInternal)
                .filter(Objects::nonNull)
                .map(this::applyTemplate)
                .filter(service -> Objects.nonNull(service)
                    && StringUtils.isNotBlank(service.getName())
                    && StringUtils.isNotBlank(service.getServiceId()))
                .collect(Collectors.toMap(service -> {
                    LOGGER.trace("Adding registered service [{}] with name [{}] and internal identifier [{}]",
                        service.getServiceId(), service.getName(), service.getId());
                    return service.getId();
                }, Function.identity(), (__, service) -> service));
            cacheRegisteredServices(servicesMap);
            loadInternal();
            val clientInfo = ClientInfoHolder.getClientInfo();
            publishEvent(new CasRegisteredServicesLoadedEvent(this, getAllServices(), clientInfo));
            evaluateExpiredServiceDefinitions();

            val cachedServices = configurationContext.getServicesCache().asMap();
            if (cachedServices.isEmpty()) {
                LOGGER.info("Loaded [{}] service(s) directly from service registry [{}].", servicesMap.size(),
                    configurationContext.getServiceRegistry().getName());
                return servicesMap.values();
            }
            LOGGER.info("Loaded [{}] service(s) from cache [{}].", cachedServices.size(),
                configurationContext.getServiceRegistry().getName());
            return cachedServices.values();
        });
    }

    private Map<Long, RegisteredService> cacheRegisteredServices(final Map<Long, RegisteredService> servicesMap) {
        val servicesCache = configurationContext.getServicesCache();
        servicesCache.invalidateAll();
        servicesCache.putAll(servicesMap);
        if (configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()) {
            indexedRegisteredServices.clear();
            indexedRegisteredServices.addAll(servicesMap.values());
        }
        return servicesCache.asMap();
    }

    @Override
    public long count() {
        return configurationContext.getServiceRegistry().size();
    }

    @Override
    public Stream<RegisteredService> findServicesBy(final RegisteredServiceQuery... queries) {
        val serviceQueries = Arrays
            .stream(queries)
            .map(RegisteredServiceQueryAttribute::new)
            .map(RegisteredServiceQueryAttribute::toQuery)
            .toList();

        if (serviceQueries.isEmpty()) {
            LOGGER.trace("No queries were provided to search for services");
            return Stream.empty();
        }

        if (!configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()) {
            val queryOptions = new QueryOptions();
            return getCacheableServicesStream()
                .get()
                .filter(Objects::nonNull)
                .filter(registeredService -> Arrays.stream(queries)
                    .filter(query -> query.getType().equals(registeredService.getClass())
                        || (query.isIncludeAssignableTypes() && query.getType().isAssignableFrom(registeredService.getClass())))
                    .findFirst()
                    .stream()
                    .anyMatch(query -> {
                        val queryAttribute = new RegisteredServiceQueryAttribute(query);
                        val propertyValue = queryAttribute.getValue(registeredService, queryOptions);
                        return query.getValue().equals(propertyValue);
                    }));
        }


        if (serviceQueries.size() == 1) {
            try (val results = indexedRegisteredServices.retrieve(serviceQueries.getFirst())) {
                return results.stream();
            }
        }
        val subQueries = serviceQueries.subList(2, serviceQueries.size());
        val query = QueryFactory.and(serviceQueries.getFirst(), serviceQueries.get(1), (Collection) subQueries);
        try (val results = indexedRegisteredServices.retrieve(query)) {
            return results.stream();
        }
    }

    protected abstract Collection<RegisteredService> getCandidateServicesToMatch(String serviceId);

    protected void deleteInternal(final RegisteredService service) {
    }

    protected void saveInternal(final RegisteredService service) {
    }

    protected void loadInternal() {
    }

    protected void loadInternal(final RegisteredService service) {
    }

    protected RegisteredService applyTemplate(final RegisteredService service) {
        return this.configurationContext.getRegisteredServicesTemplatesManager().apply(service);
    }

    protected Supplier<Stream<RegisteredService>> getCacheableServicesStream() {
        configurationContext.getServicesCache().cleanUp();
        val size = configurationContext.getServicesCache().estimatedSize();
        if (size <= 0) {
            return () -> (Stream<RegisteredService>) configurationContext.getServiceRegistry().getServicesStream();
        }
        return () -> configurationContext.getServicesCache().asMap().values().stream();
    }

    private void cacheRegisteredService(final RegisteredService service) {
        configurationContext.getServicesCache().put(service.getId(), service);
        if (configurationContext.getCasProperties().getServiceRegistry().getCore().isIndexServices()) {
            indexedRegisteredServices.removeIf(registeredService -> registeredService.getId() == service.getId());
            indexedRegisteredServices.add(service);
        }
    }

    private void evaluateExpiredServiceDefinitions() {
        getCacheableServicesStream()
            .get()
            .filter(RegisteredServiceAccessStrategyUtils.getRegisteredServiceExpirationPolicyPredicate().negate())
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
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (policy.isNotifyWhenExpired()) {
            LOGGER.debug("Contacts for registered service [{}] will be notified of service expiry", registeredService.getServiceId());
            publishEvent(new CasRegisteredServiceExpiredEvent(this, registeredService, false, clientInfo));
        }
        if (policy.isDeleteWhenExpired()) {
            LOGGER.debug("Deleting expired registered service [{}] from registry.", registeredService.getServiceId());
            if (policy.isNotifyWhenDeleted()) {
                LOGGER.debug("Contacts for registered service [{}] will be notified of service expiry and removal",
                    registeredService.getServiceId());
                publishEvent(new CasRegisteredServiceExpiredEvent(this, registeredService, true, clientInfo));
            }
            delete(registeredService);
            return null;
        }
        return registeredService;
    }

    private void publishEvent(final ApplicationEvent event) {
        configurationContext.getApplicationContext().publishEvent(event);
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
        return getCacheableServicesStream()
            .get()
            .filter(filter)
            .findFirst()
            .orElse(null);
    }

    @SafeVarargs
    private static Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(
        final Predicate<RegisteredService>... p) {
        val predicates = Stream.of(p).toList();
        return predicates.stream().reduce(x -> true, Predicate::and);
    }
}
