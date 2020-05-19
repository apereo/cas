package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.ToString;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the ServiceRegistry based on JPA.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerServiceReg")
@ToString
public class JpaServiceRegistry extends AbstractServiceRegistry {
    private static final String ENTITY_NAME = AbstractRegisteredService.class.getSimpleName();


    @PersistenceContext(unitName = "serviceEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaServiceRegistry(final ConfigurableApplicationContext applicationContext,
                              final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        if (this.entityManager.contains(registeredService)) {
            this.entityManager.remove(registeredService);
        } else {
            this.entityManager.remove(this.entityManager.merge(registeredService));
        }
        return true;
    }

    @Override
    public Collection<RegisteredService> load() {
        val query = String.format("SELECT r from %s r", ENTITY_NAME);
        val list = this.entityManager.createQuery(query, RegisteredService.class).getResultList();
        return list
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)))
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        val isNew = registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE;
        invokeServiceRegistryListenerPreSave(registeredService);
        val r = this.entityManager.merge(registeredService);
        if (!isNew) {
            this.entityManager.persist(r);
        }
        return r;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.entityManager.find(AbstractRegisteredService.class, id);
    }

    @Override
    public long size() {
        val query = String.format("SELECT count(r) from %s r", ENTITY_NAME);
        return this.entityManager.createQuery(query, Long.class).getSingleResult();
    }
}
