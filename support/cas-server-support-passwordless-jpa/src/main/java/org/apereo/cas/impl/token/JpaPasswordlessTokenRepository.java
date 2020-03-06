package org.apereo.cas.impl.token;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * This is {@link JpaPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "passwordlessTransactionManager")
@Slf4j
public class JpaPasswordlessTokenRepository extends BasePasswordlessTokenRepository {

    private static final String SELECT_QUERY = "SELECT t FROM JpaPasswordlessAuthenticationToken t ";

    private static final String DELETE_QUERY = "DELETE FROM JpaPasswordlessAuthenticationToken t ";

    private static final String QUERY_PARAM_USERNAME = "username";

    @PersistenceContext(unitName = "passwordlessEntityManagerFactory")
    private EntityManager entityManager;

    public JpaPasswordlessTokenRepository(final int tokenExpirationInSeconds) {
        super(tokenExpirationInSeconds);
    }

    @Override
    public Optional<String> findToken(final String username) {
        val query = SELECT_QUERY.concat(" WHERE t.username = :username");
        val results = this.entityManager.createQuery(query, JpaPasswordlessAuthenticationToken.class)
            .setParameter(QUERY_PARAM_USERNAME, username)
            .setMaxResults(1)
            .getResultList();
        if (!results.isEmpty()) {
            val token = results.get(0);
            if (token.isExpired()) {
                LOGGER.warn("Token [{}] has expired", token);
                return Optional.empty();
            }
            LOGGER.debug("Located token [{}]", token);
            return Optional.of(token.getToken());
        }
        return Optional.empty();
    }

    @Override
    public void deleteTokens(final String username) {
        this.entityManager.createQuery(DELETE_QUERY.concat("WHERE t.username = :username"))
            .setParameter(QUERY_PARAM_USERNAME, username)
            .executeUpdate();
    }

    @Override
    public void deleteToken(final String username, final String token) {
        val query = DELETE_QUERY.concat(" WHERE t.username = :username AND t.token = :token");
        this.entityManager.createQuery(query)
            .setParameter(QUERY_PARAM_USERNAME, username)
            .setParameter("token", token)
            .executeUpdate();
    }

    @SneakyThrows
    @Override
    public void saveToken(final String username, final String token) {
        val entity = PasswordlessAuthenticationToken.builder()
            .token(token)
            .username(username)
            .expirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTokenExpirationInSeconds()))
            .build();

        val record = new JpaPasswordlessAuthenticationToken();
        BeanUtils.copyProperties(record, entity);
        LOGGER.debug("Saving token [{}]", record);
        entityManager.merge(record);
    }

    @Override
    public void clean() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Cleaning expired records with an expiration date of [{}]", now);
        val query = DELETE_QUERY.concat(" WHERE t.expirationDate >= :expirationDate");
        this.entityManager.createQuery(query)
            .setParameter("expirationDate", now)
            .executeUpdate();
    }
}
