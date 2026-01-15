package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.Getter;
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

    public JpaServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final TransactionOperations transactionTemplate) {
        super(applicationContext, serviceRegistryListeners);
        this.transactionTemplate = transactionTemplate;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
        this.entityManager = recreateEntityManagerIfNecessary(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        transactionTemplate.executeWithoutResult(_ -> {
            val entity = fromRegisteredService(registeredService);
            if (entityManager.contains(entity)) {
                entityManager.remove(entity);
            } else {
                val mergedEntity = entityManager.merge(entity);
                entityManager.remove(mergedEntity);
            }
        });
        return true;
    }

    @Override
    public void deleteAll() {
        transactionTemplate.executeWithoutResult(_ -> {
            val query = String.format("DELETE FROM %s s", JpaRegisteredServiceEntity.ENTITY_NAME);
            entityManager.createQuery(query).executeUpdate();
        });
    }

    @Override
    public Collection<RegisteredService> load() {
        return transactionTemplate.execute(status -> {
            val query = String.format("SELECT r FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
            val list = entityManager.createQuery(query, JpaRegisteredServiceEntity.class).getResultList();
            val clientInfo = ClientInfoHolder.getClientInfo();
            return list
                .stream()
                .map(this::toRegisteredService)
                .sorted()
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .peek(service -> publishEvent(new CasRegisteredServiceLoadedEvent(this, service, clientInfo)))
                .collect(Collectors.toList());
        });
    }

    @Override
    public Long save(final Supplier<RegisteredService> supplier,
                     final Consumer<RegisteredService> andThenConsume,
                     final long countExclusive) {
        return transactionTemplate.execute(_ ->
            LongStream.range(0, countExclusive)
                .mapToObj(count -> supplier.get())
                .filter(Objects::nonNull)
                .map(this::saveInternal)
                .peek(andThenConsume)
                .filter(Objects::nonNull)
                .count());
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return transactionTemplate.execute(status -> saveInternal(registeredService));
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return transactionTemplate.execute(_ ->
            Optional.ofNullable(entityManager.find(JpaRegisteredServiceEntity.class, id))
                .map(this::toRegisteredService)
                .stream()
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .findFirst()
                .orElse(null));
    }

    @Override
    public RegisteredService findServiceBy(final String id) {
        return transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r FROM %s r WHERE r.serviceId LIKE :serviceId", JpaRegisteredServiceEntity.ENTITY_NAME);
            val results = entityManager.createQuery(query, JpaRegisteredServiceEntity.class)
                .setParameter("serviceId", '%' + id + '%')
                .getResultList();
            return results
                .stream()
                .map(this::toRegisteredService)
                .sorted()
                .filter(r -> r.matches(id))
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .findFirst()
                .orElse(null);
        });
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r FROM %s r WHERE r.serviceId=:serviceId", JpaRegisteredServiceEntity.ENTITY_NAME);
            val results = entityManager.createQuery(query, JpaRegisteredServiceEntity.class)
                .setParameter("serviceId", id)
                .getResultList();
            return results
                .stream()
                .map(this::toRegisteredService)
                .sorted()
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .findFirst()
                .orElse(null);
        });
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        return transactionTemplate.execute(_ -> {
            val query = String.format("SELECT r FROM %s r WHERE r.name=:name", JpaRegisteredServiceEntity.ENTITY_NAME);
            val results = entityManager.createQuery(query, JpaRegisteredServiceEntity.class)
                .setParameter("name", name)
                .getResultList();
            return results
                .stream()
                .map(this::toRegisteredService)
                .sorted()
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .findFirst()
                .orElse(null);
        });
    }

    @Override
    public long size() {
        return transactionTemplate.execute(status -> {
            val query = String.format("SELECT COUNT(r.id) FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
            return entityManager.createQuery(query, Long.class).getSingleResult();
        });
    }

    private RegisteredService saveInternal(final RegisteredService registeredService) {
        invokeServiceRegistryListenerPreSave(registeredService);

        val entity = fromRegisteredService(registeredService);
        val foundEntity = entityManager.find(JpaRegisteredServiceEntity.class, entity.getId());
        if (foundEntity == null) {
            try {
                entityManager.persist(entity.setId(0));
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
        val service = serializer.from(entity.getBody());
        service.setId(entity.getId());
        LOGGER.trace("Converted JPA entity [{}] to [{}]", this, service);
        return service;
    }

    @Override
    public void destroy() {
        FunctionUtils.doAndHandle(_ -> entityManager.close());
    }
}
