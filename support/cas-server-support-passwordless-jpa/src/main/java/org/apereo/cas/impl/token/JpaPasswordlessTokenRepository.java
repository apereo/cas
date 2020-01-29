package org.apereo.cas.impl.token;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Optional;

/**
 * This is {@link JpaPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "passwordlessTransactionManager")
public class JpaPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private static final String SELECT_QUERY = "SELECT t FROM PasswordlessAuthenticationToken t ";

    private static final String DELETE_QUERY = "DELETE FROM PasswordlessAuthenticationToken t WHERE t.username = :username ";

    @PersistenceContext(unitName = "passwordlessEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaPasswordlessTokenRepository(final int tokenExpirationInSeconds) {
        super(tokenExpirationInSeconds);
    }

    @Override
    public Optional<String> findToken(final String username) {
        val query = SELECT_QUERY.concat(" WHERE t.username = :username");
        val results = this.entityManager.createQuery(query, PasswordlessAuthenticationToken.class)
            .setParameter("username", username)
            .getResultList();
        if (!results.isEmpty()) {
            return Optional.of(results.get(0).getToken());
        }
        return Optional.empty();
    }

    @Override
    public void deleteTokens(final String username) {
        this.entityManager.createQuery(DELETE_QUERY, PasswordlessAuthenticationToken.class)
            .setParameter("username", username)
            .executeUpdate();
    }

    @Override
    public void deleteToken(final String username, final String token) {
        val query = DELETE_QUERY.concat(" AND t.token = :token");
        this.entityManager.createQuery(query)
            .setParameter("username", username)
            .setParameter("token", token)
            .executeUpdate();
    }

    @Override
    public void saveToken(final String username, final String token) {
        val entity = PasswordlessAuthenticationToken.builder()
            .token(token)
            .username(username)
            .build();
        entityManager.merge(entity);
    }
}
