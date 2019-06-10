package org.apereo.cas.pm.jdbc;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.impl.history.BasePasswordHistoryService;
import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * This is {@link JdbcPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerPasswordHistory")
@Slf4j
@ToString
public class JdbcPasswordHistoryService extends BasePasswordHistoryService {
    private static final String SELECT_QUERY = "SELECT p FROM PasswordHistoryEntity p ";

    @PersistenceContext(unitName = "passwordHistoryEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        val encodedPassword = encodePassword(changeRequest.getPassword());
        val query = SELECT_QUERY.concat("WHERE p.username = :username AND p.password = :password");
        return !this.entityManager.createQuery(query, PasswordHistoryEntity.class)
            .setParameter("username", changeRequest.getUsername())
            .setParameter("password", encodedPassword)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) {
        val encodedPassword = encodePassword(changeRequest.getPassword());
        val entity = new PasswordHistoryEntity();
        entity.setUsername(changeRequest.getUsername());
        entity.setPassword(encodedPassword);
        this.entityManager.merge(entity);
        return true;
    }

    @Override
    public Collection<PasswordHistoryEntity> fetchAll() {
        return this.entityManager.createQuery(SELECT_QUERY, PasswordHistoryEntity.class).getResultList();
    }

    @Override
    public Collection<PasswordHistoryEntity> fetch(final String username) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("WHERE p.username = :username"), PasswordHistoryEntity.class)
            .setParameter("username", username)
            .getResultList();
    }

    @Override
    public void remove(final String username) {
        this.entityManager.createQuery("DELETE FROM PasswordHistoryEntity p WHERE p.username = :username")
            .setParameter("username", username)
            .executeUpdate();
    }

    @Override
    public void removeAll() {
        this.entityManager.createQuery("DELETE FROM PasswordHistoryEntity p").executeUpdate();
    }
}
