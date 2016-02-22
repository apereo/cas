package org.jasig.cas.support.events.jpa;

import org.jasig.cas.support.events.dao.AbstractCasEventRepository;
import org.jasig.cas.support.events.dao.CasEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * This is {@link JpaCasEventRepository} that stores event data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Repository("casEventRepository")
@Transactional
public class JpaCasEventRepository extends AbstractCasEventRepository {

    @NotNull
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
