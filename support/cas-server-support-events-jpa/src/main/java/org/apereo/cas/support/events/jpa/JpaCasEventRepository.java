package org.apereo.cas.support.events.jpa;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.ToString;
import lombok.val;
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
@ToString
public class JpaCasEventRepository extends AbstractCasEventRepository {

    private static final String SELECT_QUERY = "SELECT r from CasEvent r ";

    @PersistenceContext(unitName = "eventsEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public void save(final CasEvent event) {
        this.entityManager.merge(event);
    }

    @Override
    public Collection<? extends CasEvent> load() {
        return this.entityManager.createQuery(SELECT_QUERY.trim(), CasEvent.class).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> load(final ZonedDateTime dateTime) {
        val query = SELECT_QUERY.concat("where r.creationTime >= :creationTime");
        return this.entityManager.createQuery(query, CasEvent.class)
            .setParameter(CREATION_TIME_PARAM, dateTime.toString()).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        val query = SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime and r.principalId = :principalId");
        return this.entityManager.createQuery(query, CasEvent.class).setParameter(TYPE_PARAM, type)
            .setParameter(PRINCIPAL_ID_PARAM, principal)
            .setParameter(CREATION_TIME_PARAM, dateTime.toString()).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        val query = SELECT_QUERY.concat("where r.type = :type and r.principalId = :principalId");
        return this.entityManager.createQuery(query, CasEvent.class).setParameter(TYPE_PARAM, type)
            .setParameter(PRINCIPAL_ID_PARAM, principal).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        val query = SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime");
        return this.entityManager.createQuery(query, CasEvent.class)
            .setParameter(TYPE_PARAM, type)
            .setParameter(CREATION_TIME_PARAM, dateTime.toString()).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("where r.type = :type"), CasEvent.class).setParameter(TYPE_PARAM, type).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        val query = SELECT_QUERY.concat("where r.principalId = :principalId and r.creationTime >= :creationTime");
        return this.entityManager.createQuery(query, CasEvent.class)
            .setParameter(PRINCIPAL_ID_PARAM, id)
            .setParameter(CREATION_TIME_PARAM, dateTime.toString()).getResultList();
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        val query = SELECT_QUERY.concat("where r.principalId = :principalId");
        return this.entityManager.createQuery(query, CasEvent.class).setParameter(PRINCIPAL_ID_PARAM, id).getResultList();
    }
}
