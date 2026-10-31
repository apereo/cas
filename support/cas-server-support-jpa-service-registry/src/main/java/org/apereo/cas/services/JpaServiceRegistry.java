package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Implementation of the ServiceRegistry based on JPA.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
@ToString
@Slf4j
public class JpaServiceRegistry extends AbstractServiceRegistry implements JpaPersistenceUnitProvider {
    /**
     * The persistence unit name.
     */
    public static final String PERSISTENCE_UNIT_NAME = "jpaServiceRegistryContext";

    private final TransactionOperations transactionTemplate;

    @Getter
    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private final StringSerializer<RegisteredService> serializer;

    @ToString.Exclude
    private final Map<Long, LoadedService> loadedServices = new ConcurrentHashMap<>();

    /**
     * Number of services saved in bulk between two flushes of the persistence context.
     */
    @Setter
    private int batchSize = 100;

    public JpaServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final TransactionOperations transactionTemplate) {
        super(applicationContext, serviceRegistryListeners);
        this.transactionTemplate = transactionTemplate;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
        this.entityManager = recreateEntityManagerIfNecessary(PERSISTENCE_UNIT_NAME);
    }

    /**
     * Removes the service with one statement, without reading it back or serializing it first.
     *
     * @param registeredService the registered service
     * @return true
     */
    @Override
    public boolean delete(final RegisteredService registeredService) {
        transactionTemplate.executeWithoutResult(_ -> {
            val query = String.format("DELETE FROM %s r WHERE r.id = :id", JpaRegisteredServiceEntity.ENTITY_NAME);
            entityManager.createQuery(query).setParameter("id", registeredService.getId()).executeUpdate();
        });
        loadedServices.remove(registeredService.getId());
        return true;
    }

    @Override
    public void deleteAll() {
        transactionTemplate.executeWithoutResult(_ -> {
            val query = String.format("DELETE FROM %s s", JpaRegisteredServiceEntity.ENTITY_NAME);
            entityManager.createQuery(query).executeUpdate();
        });
        loadedServices.clear();
    }

    @Override
    public Collection<RegisteredService> load() {
        val clientInfo = ClientInfoHolder.getClientInfo();
        return loadServices()
            .stream()
            .sorted()
            .peek(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service, clientInfo)))
            .collect(Collectors.toList());
    }

    @Override
    public Stream<? extends RegisteredService> getServicesStream() {
        return loadServices().stream();
    }

    /**
     * Bulk saves run in a single transaction. The persistence context is flushed and cleared every
     * {@code batchSize} services, so it never holds more than one batch and the inserts can be batched.
     *
     * @param supplier       the supplier of services to save
     * @param andThenConsume what to do with each saved service
     * @param countExclusive the number of services to save
     * @return the number of services saved
     */
    @Override
    public Long save(final Supplier<RegisteredService> supplier,
                     final Consumer<RegisteredService> andThenConsume,
                     final long countExclusive) {
        return transactionTemplate.execute(_ -> {
            var saved = 0L;
            for (var count = 0L; count < countExclusive; count++) {
                val registeredService = supplier.get();
                if (registeredService != null) {
                    andThenConsume.accept(saveInternal(registeredService));
                    saved++;
                    if (batchSize > 0 && saved % batchSize == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                }
            }
            return saved;
        });
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return transactionTemplate.execute(status -> saveInternal(registeredService));
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r.id, r.body FROM %s r WHERE r.id = :id", JpaRegisteredServiceEntity.ENTITY_NAME);
            return entityManager.createQuery(query, Object[].class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst()
                .map(this::toRegisteredService)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .orElse(null);
        });
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return findServiceByColumn("serviceId", id);
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        return findServiceByColumn("name", name);
    }

    @Override
    public long size() {
        return transactionTemplate.execute(status -> {
            val query = String.format("SELECT COUNT(r.id) FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
            return entityManager.createQuery(query, Long.class).getSingleResult();
        });
    }

    /**
     * Reads every stored service as its identifier and body, so no entity is loaded, kept or checked for changes.
     * A service whose body is the one read last time is not deserialized again: the service built from it then,
     * after the post-load listeners, is returned as it was. Services no longer stored are forgotten.
     *
     * @return the services
     */
    private List<RegisteredService> loadServices() {
        val services = transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r.id, r.body FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
            return entityManager.createQuery(query, Object[].class)
                .getResultList()
                .stream()
                .map(this::toLoadedService)
                .filter(Objects::nonNull)
                .toList();
        });
        val results = Objects.requireNonNullElse(services, List.<RegisteredService>of());
        val identifiers = results.stream().map(RegisteredService::getId).collect(Collectors.toSet());
        loadedServices.keySet().retainAll(identifiers);
        return results;
    }

    private RegisteredService toLoadedService(final Object[] row) {
        val id = (Long) row[0];
        val body = (String) row[1];
        val previous = loadedServices.get(id);
        if (previous != null && previous.body().equals(body)) {
            return previous.service();
        }
        val service = invokeServiceRegistryListenerPostLoad(toRegisteredService(id, body));
        if (service != null) {
            loadedServices.put(id, new LoadedService(body, service));
        } else {
            loadedServices.remove(id);
        }
        return service;
    }

    private RegisteredService findServiceByColumn(final String column, final String value) {
        return transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r.id, r.body FROM %s r WHERE r.%s = :value",
                JpaRegisteredServiceEntity.ENTITY_NAME, column);
            return entityManager.createQuery(query, Object[].class)
                .setParameter("value", value)
                .getResultList()
                .stream()
                .map(this::toRegisteredService)
                .sorted()
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .findFirst()
                .orElse(null);
        });
    }

    /**
     * Saves the service under its own identifier. A new service is given one the way other registries do,
     * a random positive value, and is stored right away since no row can hold it yet; a service that already
     * has an identifier is stored with it, merging into the row when there is one. The identifier of an
     * imported service is therefore kept.
     *
     * @param registeredService the registered service
     * @return the saved service
     */
    private RegisteredService saveInternal(final RegisteredService registeredService) {
        invokeServiceRegistryListenerPreSave(registeredService);
        val newService = registeredService.getId() == RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE;
        registeredService.assignIdIfNecessary();

        val entity = fromRegisteredService(registeredService);
        val foundEntity = newService ? null : entityManager.find(JpaRegisteredServiceEntity.class, entity.getId());
        if (foundEntity == null) {
            try {
                entityManager.persist(entity);
                return toRegisteredService(entity);
            } catch (final EntityExistsException e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        val storedEntity = entityManager.merge(entity);
        return toRegisteredService(storedEntity);
    }


    protected JpaRegisteredServiceEntity fromRegisteredService(final RegisteredService service) {
        val jsonBody = serializer.toString(service);
        val identifier = service.getId() == RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE ? 0L : service.getId();
        return JpaRegisteredServiceEntity.builder()
            .id(identifier)
            .name(service.getName())
            .serviceId(service.getServiceId())
            .evaluationOrder(service.getEvaluationOrder())
            .body(jsonBody)
            .build();
    }

    protected RegisteredService toRegisteredService(final JpaRegisteredServiceEntity entity) {
        return toRegisteredService(entity.getId(), entity.getBody());
    }

    private RegisteredService toRegisteredService(final Object[] row) {
        return toRegisteredService((Long) row[0], (String) row[1]);
    }

    private RegisteredService toRegisteredService(final long id, final String body) {
        val service = serializer.from(body);
        service.setId(id);
        LOGGER.trace("Converted JPA entity [{}] to [{}]", id, service);
        return service;
    }

    @Override
    public void destroy() {
        FunctionUtils.doAndHandle(_ -> entityManager.close());
    }

    private record LoadedService(String body, RegisteredService service) {
    }
}
