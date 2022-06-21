package org.apereo.cas.services;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Implementation of the ServiceRegistry based on JPA.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
@Slf4j
public class JpaServiceRegistry extends AbstractServiceRegistry {
    /**
     * Transaction manager name.
     */
    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerServiceReg";

    private final TransactionOperations transactionTemplate;

    @PersistenceContext(unitName = "serviceEntityManagerFactory")
    private EntityManager entityManager;

    private final StringSerializer<RegisteredService> serializer;

    public JpaServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final TransactionOperations transactionTemplate) {
        super(applicationContext, serviceRegistryListeners);
        this.transactionTemplate = transactionTemplate;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val entity = fromRegisteredService(registeredService);

        if (entityManager.contains(entity)) {
            entityManager.remove(entity);
        } else {
            entityManager.remove(entityManager.merge(entity));
        }
        return true;
    }

    @Override
    public void deleteAll() {
        val query = String.format("DELETE FROM %s s", JpaRegisteredServiceEntity.ENTITY_NAME);
        entityManager.createQuery(query).executeUpdate();
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public Collection<RegisteredService> load() {
        val query = String.format("SELECT r FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
        val list = entityManager.createQuery(query, JpaRegisteredServiceEntity.class).getResultList();
        return list
            .stream()
            .map(this::toRegisteredService)
            .sorted()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)))
            .collect(Collectors.toList());
    }

    @Override
    public Long save(final Supplier<RegisteredService> supplier,
                     final Consumer<RegisteredService> andThenConsume,
                     final long countExclusive) {
        return transactionTemplate.execute(status ->
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
        return this.transactionTemplate.execute(status -> saveInternal(registeredService));
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public RegisteredService findServiceById(final long id) {
        return Optional.ofNullable(this.entityManager.find(JpaRegisteredServiceEntity.class, id))
            .map(this::toRegisteredService)
            .stream()
            .peek(this::invokeServiceRegistryListenerPostLoad)
            .findFirst()
            .orElse(null);
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public RegisteredService findServiceBy(final String id) {
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
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public RegisteredService findServiceByExactServiceId(final String id) {
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
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public RegisteredService findServiceByExactServiceName(final String name) {
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
    }

    @Override
    @Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, readOnly = true)
    public long size() {
        val query = String.format("SELECT COUNT(r.id) FROM %s r", JpaRegisteredServiceEntity.ENTITY_NAME);
        return this.entityManager.createQuery(query, Long.class).getSingleResult();
    }

    private RegisteredService saveInternal(final RegisteredService registeredService) {
        val isNew = registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE;
        invokeServiceRegistryListenerPreSave(registeredService);

        val entity = fromRegisteredService(registeredService);
        if (isNew) {
            entityManager.persist(entity);
            return toRegisteredService(entity);
        }
        val r = entityManager.merge(entity);
        return toRegisteredService(r);
    }


    /**
     * From registered service.
     *
     * @param service the service
     * @return the jpa registered service entity
     */
    private JpaRegisteredServiceEntity fromRegisteredService(final RegisteredService service) {
        val jsonBody = serializer.toString(service);
        return JpaRegisteredServiceEntity.builder()
            .id(service.getId())
            .name(service.getName())
            .serviceId(service.getServiceId())
            .evaluationOrder(service.getEvaluationOrder())
            .body(jsonBody)
            .build();
    }

    private RegisteredService toRegisteredService(final JpaRegisteredServiceEntity entity) {
        val service = serializer.from(entity.getBody());
        service.setId(entity.getId());
        LOGGER.trace("Converted JPA entity [{}] to [{}]", this, service);
        return service;
    }
}
