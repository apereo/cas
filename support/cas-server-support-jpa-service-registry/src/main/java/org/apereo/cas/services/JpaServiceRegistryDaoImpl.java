package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Implementation of the ServiceRegistryDao based on JPA.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerServiceReg", readOnly = false)
public class JpaServiceRegistryDaoImpl extends AbstractServiceRegistryDao {

    @PersistenceContext(unitName = "serviceEntityManagerFactory")
    private EntityManager entityManager;

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
    public List<RegisteredService> load() {
        final List<RegisteredService> list = this.entityManager
                .createQuery("select r from AbstractRegisteredService r", RegisteredService.class).getResultList();
        list.stream().forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
        return list;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        final boolean isNew = registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE;
        final RegisteredService r = this.entityManager.merge(registeredService);
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
    public RegisteredService findServiceById(final String id) {
        return load().stream().filter(r -> r.matches(id)).findFirst().orElse(null);
    }


    @Override
    public long size() {
        return this.entityManager.createQuery("select count(r) from AbstractRegisteredService r", Long.class).getSingleResult();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
