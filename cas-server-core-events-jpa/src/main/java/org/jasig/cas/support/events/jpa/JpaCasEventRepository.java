package org.jasig.cas.support.events.jpa;

import org.jasig.cas.support.events.dao.AbstractCasEventRepository;
import org.jasig.cas.support.events.dao.CasEventDTO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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

    /**
     * Initialized post construction.
     */
    @PostConstruct
    public void init() {}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void save(final CasEventDTO event) {
        this.entityManager.merge(event);
    }

    @Override
    public Collection<CasEventDTO> load() {
        return this.entityManager.createQuery("SELECT r FROM CasEventDTO r", CasEventDTO.class).getResultList();
    }
}
