package org.apereo.cas.support.events.jpa;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * This is {@link JpaCasEventRepository} that stores event data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerEvents")
public class JpaCasEventRepository extends AbstractCasEventRepository {
    
    @PersistenceContext(unitName = "eventsEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    @Override
    public void save(final CasEvent event) {
        this.entityManager.merge(event);
    }

    @Override
    public Collection<CasEvent> load() {
        return this.entityManager.createQuery("SELECT r FROM CasEvent r", CasEvent.class).getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        return this.entityManager.createQuery("select r from CasEvent r where r.principalId = :principalId",
                CasEvent.class).setParameter("principalId", id).getResultList();
    }
}
