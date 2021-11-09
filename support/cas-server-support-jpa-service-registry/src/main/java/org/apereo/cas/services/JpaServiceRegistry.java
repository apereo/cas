package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.ToString;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
@EnableTransactionManagement
@Transactional(transactionManager = JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER)
@ToString
public class JpaServiceRegistry extends AbstractServiceRegistry {
    /**
     * Transaction manager name.
     */
    public static final String BEAN_NAME_TRANSACTION_MANAGER = "transactionManagerServiceReg";

    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "serviceEntityManagerFactory")
    private EntityManager entityManager;

    public JpaServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners,
                              final TransactionTemplate transactionTemplate) {
        super(applicationContext, serviceRegistryListeners);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val entity = JpaRegisteredServiceEntity.fromRegisteredService(registeredService);

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
        val list = this.entityManager.createQuery(query, JpaRegisteredServiceEntity.class).getResultList();
        return list
            .stream()
            .map(JpaRegisteredServiceEntity::toRegisteredService)
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
            .map(JpaRegisteredServiceEntity::toRegisteredService)
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
            .map(JpaRegisteredServiceEntity::toRegisteredService)
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
            .map(JpaRegisteredServiceEntity::toRegisteredService)
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
            .map(JpaRegisteredServiceEntity::toRegisteredService)
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

        val entity = JpaRegisteredServiceEntity.fromRegisteredService(registeredService);
        if (isNew) {
            entityManager.persist(entity);
            return entity.toRegisteredService();
        }
        val r = entityManager.merge(entity);
        return r.toRegisteredService();
    }
}
