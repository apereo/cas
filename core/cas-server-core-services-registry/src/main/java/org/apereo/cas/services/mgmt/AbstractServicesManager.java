package org.apereo.cas.services.mgmt;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.CacheableServicesManager;
import org.apereo.cas.services.IndexableServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceChainingAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link AbstractServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public abstract class AbstractServicesManager implements IndexableServicesManager, CacheableServicesManager {
    protected final ServicesManagerConfigurationContext configurationContext;

    protected final CasReentrantLock lock = new CasReentrantLock();

    @Getter(AccessLevel.NONE)
    private final AtomicLong servicesCacheVersion = new AtomicLong();

    @Getter(AccessLevel.NONE)
    private volatile @Nullable SortedServicesSnapshot sortedServicesSnapshot;

    @Getter(AccessLevel.NONE)
    private final Set<String> notifiedExpiredServices = ConcurrentHashMap.newKeySet();

    protected AbstractServicesManager(final ServicesManagerConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        configurationContext.getRegisteredServiceIndexService().initialize();
    }

    @Override
    public void clearIndexedServices() {
        configurationContext.getRegisteredServiceIndexService().clear();
    }

    @Override
    public long countIndexedServices() {
        return configurationContext.getRegisteredServiceIndexService().count();
    }

    @Override
    public Optional<RegisteredService> findIndexedServiceBy(final long id) {
        return configurationContext.getRegisteredServiceIndexService().findServiceBy(id);
    }

    @Override
    public void save(final Stream<? extends RegisteredService> toSave) {
        toSave.forEach(this::save);
    }

    @Override
    public @Nullable RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        return lock.executeAndThrow(() -> {
            if (supports(registeredService)) {
                val clientInfo = ClientInfoHolder.getClientInfo();
                publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService, clientInfo));
                flattenAttributeReleasePolicy(registeredService);
                verifyServiceDefinition(registeredService);
                val savedService = configurationContext.getServiceRegistry().save(registeredService);
                cacheRegisteredService(savedService);
                saveInternal(registeredService);

                if (publishEvent) {
                    publishEvent(new CasRegisteredServiceSavedEvent(this, savedService, clientInfo));
                }
                return savedService;
            }
            return null;
        });
    }
    

    @Override
    public void save(final Supplier<RegisteredService> supplier,
                     final Consumer<RegisteredService> andThenConsume,
                     final long countExclusive) {
        configurationContext.getServiceRegistry().save(() -> {
            val registeredService = supplier.get();
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (registeredService != null && supports(registeredService)) {
                publishEvent(new CasRegisteredServicePreSaveEvent(this, registeredService, clientInfo));
                flattenAttributeReleasePolicy(registeredService);
                verifyServiceDefinition(registeredService);
                cacheRegisteredService(registeredService);
                saveInternal(registeredService);
                publishEvent(new CasRegisteredServiceSavedEvent(this, registeredService, clientInfo));
                return registeredService;
            }
            return null;
        }, andThenConsume, countExclusive);
    }

    @Override
    public void removeRegisteredServicesFromCache() {
        lock.executeAndThrow(() -> {
            configurationContext.getServicesCache().asMap().forEach((key, v) -> delete(v));
            configurationContext.getServicesCache().invalidateAll();
            val clientInfo = ClientInfoHolder.getClientInfo();
            publishEvent(new CasRegisteredServicesDeletedEvent(this, clientInfo));
            return null;
        });
    }

    @Override
    public void deleteAll() {
        removeRegisteredServicesFromCache();
    }

    @Override
    public @Nullable RegisteredService delete(final long id) {
        return lock.executeAndThrow(() -> {
            val service = findServiceBy(id);
            return delete(service);
        });
    }

    @Override
    public @Nullable RegisteredService delete(
        @Nullable final RegisteredService service) {
        return lock.executeAndThrow(() -> {
            if (service != null) {
                val clientInfo = ClientInfoHolder.getClientInfo();
                publishEvent(new CasRegisteredServicePreDeleteEvent(this, service, clientInfo));
                configurationContext.getServiceRegistry().delete(service);
                removeRegisteredServiceFromCache(service);
                deleteInternal(service);
                publishEvent(new CasRegisteredServiceDeletedEvent(this, service, clientInfo));
            }
            return service;
        });
    }

    @Override
    public void removeRegisteredServiceFromCache(final RegisteredService service) {
        configurationContext.getServicesCache().invalidate(service.getId());
        servicesCacheVersion.incrementAndGet();
    }

    
    @Override
    public @Nullable RegisteredService findServiceBy(@Nullable final Service service) {
        if (service == null) {
            return null;
        }
        val maximumLength = configurationContext.getCasProperties().getServiceRegistry().getCore().getMaximumServiceIdLength();
        if (maximumLength > 0 && StringUtils.length(service.getId()) > maximumLength) {
            LOGGER.warn("Service identifier of length [{}] exceeds the maximum allowed length of [{}] and will not be matched",
                StringUtils.length(service.getId()), maximumLength);
            return null;
        }

        val candidates = getCandidateServicesToMatch(service.getId());
        var foundService = configurationContext.getRegisteredServiceLocators()
            .stream()
            .map(locator -> locator.locate(candidates, service))
            .filter(registeredService -> validateRegisteredService(registeredService) != null)
            .findFirst();

        if (foundService.isEmpty() && isServiceRegistryLookupRequired()) {
            val serviceRegistry = configurationContext.getServiceRegistry();
            LOGGER.trace("Service [{}] is not cached; Searching [{}]", service.getId(), serviceRegistry.getName());
            foundService = Optional.ofNullable(prepareServiceFromRegistry(serviceRegistry.findServiceBy(service.getId())))
                .flatMap(registeredService -> configurationContext.getRegisteredServiceLocators().stream()
                    .filter(locator -> locator.supports(registeredService, service))
                    .findFirst()
                    .map(locator -> {
                        LOGGER.debug("Service [{}] is found in service registry and can be supported by [{}]", registeredService, locator.getName());
                        cacheRegisteredService(registeredService);
                        LOGGER.trace("Service [{}] is now cached from [{}]", service, serviceRegistry.getName());
                        return registeredService;
                    }));
        }

        foundService.ifPresent(RegisteredService::initialize);
        return validateRegisteredService(foundService.orElse(null));
    }

    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> predicate) {
        if (predicate == null) {
            return new ArrayList<>();
        }
        return configurationContext.getServiceRegistry().findServicePredicate(predicate)
            .stream()
            .sorted()
            .peek(RegisteredService::initialize)
            .collect(Collectors.toMap(RegisteredService::getId, Function.identity(), (r, s) -> s))
            .values();
    }

    @Override
    public <T extends RegisteredService> @Nullable T findServiceBy(final Service requestedService, final Class<T> clazz) {
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
    public @Nullable RegisteredService findServiceBy(final long id) {
        val result = findCachedRegisteredService(id, RegisteredService.class);
        return validateRegisteredService(result);
    }

    @Override
    public <T extends RegisteredService> @Nullable T findServiceBy(final long id, final Class<T> clazz) {
        val service = findCachedRegisteredService(id, clazz);
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) validateRegisteredService(service);
        }
        return null;
    }

    @Override
    public RegisteredService findCachedRegisteredService(final Long key, final Class<? extends RegisteredService> clazz) {
        return configurationContext.getServicesCache().get(key, _ -> {
            val registeredService = prepareServiceFromRegistry(configurationContext.getServiceRegistry().findServiceById(key, clazz));
            if (registeredService != null) {
                configurationContext.getRegisteredServiceIndexService().indexService(registeredService);
            }
            return registeredService;
        });
    }

    @Override
    public @Nullable RegisteredService findServiceByName(final String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        var service = getServicesSnapshot().servicesByName().get(name);
        if (service == null) {
            val registry = configurationContext.getServiceRegistry();
            LOGGER.trace("The service with name [{}] is not found in the cache; trying to find it from [{}]", name, registry.getName());
            service = prepareServiceFromRegistry(registry.findServiceByExactServiceName(name));
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
        var service = getServicesSnapshot().servicesByName().get(name);
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) service;
        }
        LOGGER.trace("The service with name [{}] and type [{}] is not found in the cache; trying to find it from [{}]",
            name, clazz, configurationContext.getServiceRegistry().getName());
        service = prepareServiceFromRegistry(configurationContext.getServiceRegistry().findServiceByExactServiceName(name, clazz));
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
            .filter(service -> checkServiceExpirationPolicyIfAny(service) != null)
            .filter(getRegisteredServicesFilteringPredicate())
            .sorted()
            .peek(RegisteredService::initialize)
            .peek(this::cacheRegisteredServiceIfAbsent)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getAllServicesOfType(final Class clazz) {
        if (supports(clazz)) {
            return getCacheableServicesStream()
                .get()
                .filter(service -> clazz.isAssignableFrom(service.getClass()))
                .filter(this::validateAndFilterServiceByEnvironment)
                .filter(service -> checkServiceExpirationPolicyIfAny(service) != null)
                .filter(getRegisteredServicesFilteringPredicate())
                .sorted()
                .peek(RegisteredService::initialize)
                .peek(this::cacheRegisteredServiceIfAbsent)
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
        return lock.executeAndThrow(() -> {
            LOGGER.trace("Loading services from [{}]", configurationContext.getServiceRegistry().getName());
            val loadedServices = loadServicesFromRegistry();
            if (loadedServices.isEmpty()) {
                return getCachedRegisteredServices().values();
            }
            val servicesMap = loadedServices.get()
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

            val cachedServices = getCachedRegisteredServices();
            if (cachedServices.isEmpty()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Loaded [{}] service(s) directly from service registry [{}].",
                        countLoadedServices(servicesMap),
                        configurationContext.getServiceRegistry().getName());
                }
                return servicesMap.values();
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Loaded [{}] service(s) from cache [{}].",
                    countLoadedServices(cachedServices),
                    configurationContext.getServiceRegistry().getName());
            }
            return cachedServices.values();
        });
    }

    /**
     * Load services from the registry. A registry that fails to load leaves the
     * services already cached in place, rather than replacing them with nothing.
     */
    private Optional<Collection<RegisteredService>> loadServicesFromRegistry() {
        val serviceRegistry = configurationContext.getServiceRegistry();
        try {
            return Optional.of(Objects.requireNonNull(serviceRegistry.load(),
                () -> "Service registry " + serviceRegistry.getName() + " returned no services"));
        } catch (final Exception e) {
            LOGGER.error("Unable to load services from [{}]; keeping the [{}] service(s) already loaded",
                serviceRegistry.getName(), getCachedRegisteredServices().size());
            LoggingUtils.error(LOGGER, e);
            return Optional.empty();
        }
    }

    @Override
    public Map<Long, RegisteredService> getCachedRegisteredServices() {
        return configurationContext.getServicesCache().asMap();
    }

    private static long countLoadedServices(final Map<Long, RegisteredService> servicesMap) {
        return servicesMap
            .values()
            .stream()
            .filter(Predicate.not(RegisteredService::isInternal))
            .count();
    }

    /**
     * Replace the cached services with the given ones. New and updated services are added
     * before the ones that are gone are removed, so that lookups running during a reload
     * never see an empty or partial set of services.
     */
    @Override
    public Map<Long, RegisteredService> cacheRegisteredServices(final Map<Long, RegisteredService> servicesMap) {
        val servicesCache = configurationContext.getServicesCache();
        servicesCache.putAll(servicesMap);
        servicesCache.asMap().keySet().retainAll(servicesMap.keySet());

        configurationContext.getRegisteredServiceIndexService().indexServices(servicesMap.values());
        servicesCacheVersion.incrementAndGet();
        return servicesCache.asMap();
    }

    @Override
    public long count() {
        return configurationContext.getServiceRegistry().size();
    }

    @Override
    public Stream<RegisteredService> findServicesBy(final RegisteredServiceQuery... queries) {
        if (configurationContext.getRegisteredServiceIndexService().isEnabled()) {
            return configurationContext.getRegisteredServiceIndexService().findServiceBy(queries);
        }
        return getCacheableServicesStream()
            .get()
            .filter(Objects::nonNull)
            .filter(registeredService -> Arrays.stream(queries)
                .filter(query -> query.getType().equals(registeredService.getClass())
                    || (query.isIncludeAssignableTypes() && query.getType().isAssignableFrom(registeredService.getClass())))
                .findFirst()
                .stream()
                .anyMatch(query -> configurationContext.getRegisteredServiceIndexService().matches(registeredService, query)));
    }

    protected abstract Collection<RegisteredService> getCandidateServicesToMatch(String serviceId);

    /**
     * Services in evaluation order, sorted once per cache change instead of once per lookup.
     * Expiry and size eviction remove entries without bumping the version, so the size is compared too.
     *
     * @return the sorted, unmodifiable services
     */
    protected List<RegisteredService> getSortedRegisteredServices() {
        return getServicesSnapshot().services();
    }

    private SortedServicesSnapshot getServicesSnapshot() {
        val version = servicesCacheVersion.get();
        val services = getCacheableServicesStream();
        val snapshot = sortedServicesSnapshot;
        if (snapshot != null && snapshot.version() == version
            && (!isServicesCacheEnabled() || snapshot.services().size() == getCachedRegisteredServicesSize())) {
            return snapshot;
        }
        val sortedServices = services.get().sorted().toList();
        val servicesByName = new HashMap<String, RegisteredService>();
        sortedServices.stream()
            .filter(service -> service.getName() != null)
            .forEach(service -> servicesByName.putIfAbsent(service.getName(), service));
        val newSnapshot = new SortedServicesSnapshot(version, sortedServices, Map.copyOf(servicesByName));
        sortedServicesSnapshot = newSnapshot;
        return newSnapshot;
    }

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

    /**
     * Prepare a service read directly from the registry, outside a load, the same way a load does,
     * so that a service fetched on demand carries the settings its templates supply.
     *
     * @param registeredService the service read from the registry
     * @return the prepared service, or null if this manager does not support it
     */
    protected @Nullable RegisteredService prepareServiceFromRegistry(@Nullable final RegisteredService registeredService) {
        if (registeredService == null || !supports(registeredService)) {
            return null;
        }
        loadInternal(registeredService);
        return applyTemplate(registeredService);
    }

    protected Supplier<Stream<RegisteredService>> getCacheableServicesStream() {
        cleanRegisteredServicesCache();
        val size = getCachedRegisteredServicesSize();
        if (size <= 0) {
            return () -> (Stream<RegisteredService>) configurationContext.getServiceRegistry().getServicesStream();
        }
        return () -> configurationContext.getServicesCache().asMap().values().stream();
    }

    @Override
    public long getCachedRegisteredServicesSize() {
        return configurationContext.getServicesCache().estimatedSize();
    }

    @Override
    public void cleanRegisteredServicesCache() {
        configurationContext.getServicesCache().cleanUp();
    }

    @Override
    public void cacheRegisteredService(final RegisteredService service) {
        configurationContext.getServicesCache().put(service.getId(), service);
        configurationContext.getRegisteredServiceIndexService().indexService(service);
        servicesCacheVersion.incrementAndGet();
    }

    private void cacheRegisteredServiceIfAbsent(final RegisteredService service) {
        if (configurationContext.getServicesCache().asMap().putIfAbsent(service.getId(), service) == null) {
            configurationContext.getRegisteredServiceIndexService().indexService(service);
            servicesCacheVersion.incrementAndGet();
        }
    }

    private boolean isServicesCacheEnabled() {
        return configurationContext.getCasProperties().getServiceRegistry().getCache().getCacheSize() > 0;
    }

    private boolean isServiceRegistryLookupRequired() {
        val serviceRegistryProperties = configurationContext.getCasProperties().getServiceRegistry();
        val schedule = serviceRegistryProperties.getSchedule();
        if (!schedule.isEnabled()) {
            LOGGER.debug("Service registry is not reloaded on a schedule; unmatched services will be looked up in the registry");
            return true;
        }
        val cache = serviceRegistryProperties.getCache();
        if (cache.getCacheSize() <= 0) {
            LOGGER.debug("Services cache is disabled; unmatched services will be looked up in the registry");
            return true;
        }
        return false;
    }

    /**
     * Notify and delete expired services once per load. Lookups only filter expired services,
     * so that requests for an expired service do not notify contacts or delete definitions.
     */
    private void evaluateExpiredServiceDefinitions() {
        val expiredServices = getCacheableServicesStream()
            .get()
            .filter(RegisteredServiceAccessStrategyUtils.getRegisteredServiceExpirationPolicyPredicate().negate())
            .toList();
        expiredServices.forEach(this::processExpiredRegisteredService);
        val currentlyExpired = expiredServices.stream()
            .map(AbstractServicesManager::getExpiryNotificationKey)
            .collect(Collectors.toSet());
        notifiedExpiredServices.retainAll(currentlyExpired);
    }

    private static String getExpiryNotificationKey(final RegisteredService registeredService) {
        return registeredService.getId() + ":" + registeredService.getExpirationPolicy().getExpirationDate();
    }

    private @Nullable RegisteredService validateRegisteredService(
        @Nullable final RegisteredService registeredService) {
        val result = checkServiceExpirationPolicyIfAny(registeredService);
        if (validateAndFilterServiceByEnvironment(result)) {
            return result;
        }
        return null;
    }

    private @Nullable RegisteredService checkServiceExpirationPolicyIfAny(
        @Nullable final RegisteredService registeredService) {
        if (registeredService == null || RegisteredServiceAccessStrategyUtils.ensureServiceIsNotExpired(registeredService)) {
            return registeredService;
        }
        LOGGER.debug("Registered service [{}] has expired on [{}]", registeredService.getServiceId(),
            registeredService.getExpirationPolicy().getExpirationDate());
        return registeredService.getExpirationPolicy().isDeleteWhenExpired() ? null : registeredService;
    }

    private void processExpiredRegisteredService(final RegisteredService registeredService) {
        val policy = registeredService.getExpirationPolicy();
        val firstNotice = notifiedExpiredServices.add(getExpiryNotificationKey(registeredService));
        if (firstNotice) {
            LOGGER.warn("Registered service [{}] has expired on [{}]", registeredService.getServiceId(), policy.getExpirationDate());
        }
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (policy.isNotifyWhenExpired() && firstNotice) {
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
        }
    }

    private void publishEvent(final ApplicationEvent event) {
        configurationContext.getApplicationContext().publishEvent(event);
    }

    private boolean validateAndFilterServiceByEnvironment(
        @Nullable final RegisteredService service) {
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

    @SafeVarargs
    private static Predicate<RegisteredService> getRegisteredServicesFilteringPredicate(
        final Predicate<RegisteredService>... predicates) {
        return Stream.of(predicates).reduce(x -> true, Predicate::and);
    }

    private static void flattenAttributeReleasePolicy(final RegisteredService registeredService) {
        if (registeredService.getAttributeReleasePolicy() instanceof final RegisteredServiceChainingAttributeReleasePolicy chain
            && chain.getPolicies().size() == 1) {
            registeredService.setAttributeReleasePolicy(chain.getPolicies().getFirst());
        }
    }

    private static void verifyServiceDefinition(final RegisteredService registeredService) {
        if (StringUtils.isBlank(registeredService.getName())) {
            registeredService.setName("Service-" + registeredService.getId());
            LOGGER.warn("Registered service with id [{}] and service id [{}] is not assigned a name. "
                    + "This is a required field and failure to set this field correctly may lead to "
                    + "unpredictable behavior. For now, CAS has automatically assigned a name to this service: [{}]."
                    + "To remove this warning, please consider assigning a name to the application definition directly.",
                registeredService.getId(), registeredService.getServiceId(), registeredService.getName());
        }
    }

    private record SortedServicesSnapshot(long version, List<RegisteredService> services,
                                          Map<String, RegisteredService> servicesByName) {
    }
}
