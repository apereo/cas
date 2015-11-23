package org.jasig.cas.services;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Implementation of the ServiceRegistryDao based on JPA.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("jpaServiceRegistryDao")
public final class JpaServiceRegistryDaoImpl implements ServiceRegistryDao {
    @NotNull
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
        return this.entityManager.createQuery("select r from AbstractRegisteredService r", RegisteredService.class)
                .getResultList();
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
    public String toString() {
        return getClass().getSimpleName();
    }
}
