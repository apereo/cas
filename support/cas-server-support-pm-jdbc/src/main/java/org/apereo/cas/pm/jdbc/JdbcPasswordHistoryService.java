package org.apereo.cas.pm.jdbc;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.impl.history.BasePasswordHistoryService;
import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;

import lombok.ToString;
import lombok.val;
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
@Transactional(transactionManager = "transactionManagerPasswordHistory")
@ToString
public class JdbcPasswordHistoryService extends BasePasswordHistoryService {
    private static final String SELECT_QUERY = "SELECT p FROM JdbcPasswordHistoryEntity p ";

    @PersistenceContext(unitName = "passwordHistoryEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        val encodedPassword = encodePassword(changeRequest.getPassword());
        val query = SELECT_QUERY.concat("WHERE p.username = :username AND p.password = :password");
        return !this.entityManager.createQuery(query, JdbcPasswordHistoryEntity.class)
            .setParameter("username", changeRequest.getUsername())
            .setParameter("password", encodedPassword)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) {
        val encodedPassword = encodePassword(changeRequest.getPassword());
        val entity = new JdbcPasswordHistoryEntity();
        entity.setUsername(changeRequest.getUsername());
        entity.setPassword(encodedPassword);
        this.entityManager.merge(entity);
        return true;
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetchAll() {
        return this.entityManager.createQuery(SELECT_QUERY, JdbcPasswordHistoryEntity.class).getResultList();
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetch(final String username) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("WHERE p.username = :username"), JdbcPasswordHistoryEntity.class)
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
        this.entityManager.createQuery("DELETE FROM JdbcPasswordHistoryEntity p").executeUpdate();
    }
}
