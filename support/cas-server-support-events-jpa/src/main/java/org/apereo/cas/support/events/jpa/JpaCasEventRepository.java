package org.apereo.cas.support.events.jpa;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link JpaCasEventRepository} that stores event data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerEvents")
public class JpaCasEventRepository extends AbstractCasEventRepository {

    private static final String SELECT_QUERY = "SELECT r from CasEvent r ";

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
        return this.entityManager.createQuery(SELECT_QUERY.trim(), CasEvent.class).getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("where r.principalId = :principalId"),
                CasEvent.class).setParameter("principalId", id).getResultList();
    }

    @Override
    public Collection<CasEvent> load(final ZonedDateTime dateTime) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("where r.creationTime >= :creationTime"),
                CasEvent.class).setParameter("creationTime", dateTime.toString()).getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime and r.principalId = :principalId"),
                CasEvent.class)
                .setParameter("type", type)
                .setParameter("principalId", principal)
                .setParameter("creationTime", dateTime.toString())
                .getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime"), CasEvent.class)
                .setParameter("type", type)
                .setParameter("creationTime", dateTime.toString())
                .getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.principalId = :principalId and r.creationTime >= :creationTime"), CasEvent.class)
                .setParameter("principalId", id)
                .setParameter("creationTime", dateTime.toString())
                .getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type) {
        return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.type = :type"), CasEvent.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.type = :type and r.principalId = :principalId"),
                CasEvent.class)
                .setParameter("type", type)
                .setParameter("principalId", principal)
                .getResultList();
    }
}
