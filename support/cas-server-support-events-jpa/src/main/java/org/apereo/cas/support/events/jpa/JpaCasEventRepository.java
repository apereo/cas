package org.apereo.cas.support.events.jpa;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import lombok.ToString;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

/**
 * This is {@link JpaCasEventRepository} that stores event data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = CasEventRepository.TRANSACTION_MANAGER_EVENTS)
@ToString
public class JpaCasEventRepository extends AbstractCasEventRepository {

    private static final String SELECT_QUERY = "SELECT r from JpaCasEvent r ";

    private final PlatformTransactionManager transactionManager;

    private final CasConfigurationProperties casProperties;

    private final JpaBeanFactory jpaBeanFactory;

    @PersistenceContext(unitName = "jpaEventRegistryContext")
    private EntityManager entityManager;

    public JpaCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                 final PlatformTransactionManager transactionManager,
                                 final CasConfigurationProperties casProperties,
                                 final JpaBeanFactory jpaBeanFactory) {
        super(eventRepositoryFilter);
        this.transactionManager = transactionManager;
        this.casProperties = casProperties;
        this.jpaBeanFactory = jpaBeanFactory;
    }

    @Override
    public void removeAll() {
        entityManager.createQuery("DELETE FROM JpaCasEvent e").executeUpdate();
    }

    @Override
    public Stream<? extends CasEvent> load() {
        val query = entityManager.createQuery(SELECT_QUERY.trim(), JpaCasEvent.class);
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> load(final ZonedDateTime dateTime) {
        val sql = SELECT_QUERY.concat("where r.creationTime >= :creationTime");
        val query = entityManager.createQuery(sql, JpaCasEvent.class)
            .setParameter(CREATION_TIME_PARAM, dateTime.toInstant());
        query.setLockMode(LockModeType.NONE);
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        val sql = SELECT_QUERY.concat("where r.type = :type and r.principalId = :principalId");
        val query = entityManager.createQuery(sql, JpaCasEvent.class).setParameter(TYPE_PARAM, type)
            .setParameter(PRINCIPAL_ID_PARAM, principal);
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        val sql = SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime and r.principalId = :principalId");
        val query = entityManager.createQuery(sql, JpaCasEvent.class).setParameter(TYPE_PARAM, type)
            .setParameter(PRINCIPAL_ID_PARAM, principal)
            .setParameter(CREATION_TIME_PARAM, dateTime.toInstant());
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type) {
        val query = entityManager.createQuery(SELECT_QUERY.concat("where r.type = :type"), JpaCasEvent.class)
            .setParameter(TYPE_PARAM, type);
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        val sql = SELECT_QUERY.concat("where r.type = :type and r.creationTime >= :creationTime");
        val query = entityManager.createQuery(sql, JpaCasEvent.class)
            .setParameter(TYPE_PARAM, type)
            .setParameter(CREATION_TIME_PARAM, dateTime.toInstant());
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        val sql = SELECT_QUERY.concat("where r.principalId = :principalId");
        val query = entityManager.createQuery(sql, JpaCasEvent.class)
            .setParameter(PRINCIPAL_ID_PARAM, id);
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        val sql = SELECT_QUERY.concat("where r.principalId = :principalId and r.creationTime >= :creationTime");
        var query = entityManager.createQuery(sql, JpaCasEvent.class)
            .setParameter(PRINCIPAL_ID_PARAM, id)
            .setParameter(CREATION_TIME_PARAM, dateTime.toInstant());
        return jpaBeanFactory
            .streamQuery(query)
            .map(JpaCasEvent.class::cast)
            .map(CasEvent::from);
    }

    @Override
    public CasEvent saveInternal(final CasEvent event) {
        val transactionTemplate = new TransactionTemplate(this.transactionManager);
        return transactionTemplate.execute((TransactionCallback<CasEvent>) ts -> {
            val jpaEvent = new JpaCasEvent();
            jpaEvent.setId(event.getId());
            jpaEvent.setCreationTime(event.getCreationTime());
            jpaEvent.setPrincipalId(event.getPrincipalId());
            jpaEvent.setProperties(event.getProperties());
            jpaEvent.setType(event.getType());
            return entityManager.merge(jpaEvent);
        });
    }

    @Override
    public <T> void withTransaction(final CheckedConsumer<T> action) {
        val transactionTemplate = new TransactionTemplate(this.transactionManager);
        transactionTemplate.executeWithoutResult(Unchecked.consumer(ts -> action.accept(null)));
    }
}
