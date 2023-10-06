package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * This is {@link JpaPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "passwordlessTransactionManager")
@Slf4j
public class JpaPasswordlessTokenRepository extends BasePasswordlessTokenRepository {

    private static final String SELECT_QUERY = String.format("SELECT t FROM %s t ", JpaPasswordlessAuthenticationEntity.class.getSimpleName());

    private static final String DELETE_QUERY = String.format("DELETE FROM %s t ", JpaPasswordlessAuthenticationEntity.class.getSimpleName());

    private static final String QUERY_PARAM_USERNAME = "username";

    @PersistenceContext(unitName = "jpaPasswordlessAuthNContext")
    private EntityManager entityManager;

    public JpaPasswordlessTokenRepository(final long tokenExpirationInSeconds,
                                          final CipherExecutor cipherExecutor) {
        super(tokenExpirationInSeconds, cipherExecutor);
    }

    @Override
    public Optional<PasswordlessAuthenticationToken> findToken(final String username) {
        val query = SELECT_QUERY.concat(" WHERE t.username = :username");
        val results = entityManager.createQuery(query, JpaPasswordlessAuthenticationEntity.class)
            .setParameter(QUERY_PARAM_USERNAME, username)
            .setMaxResults(1)
            .getResultList();
        if (!results.isEmpty()) {
            val token = results.getFirst();
            val authnToken = decodePasswordlessAuthenticationToken(token.getToken());
            if (authnToken.isExpired()) {
                LOGGER.warn("Token [{}] has expired", token);
                return Optional.empty();
            }
            LOGGER.debug("Located token [{}]", authnToken);
            return Optional.of(authnToken);
        }
        return Optional.empty();
    }

    @Override
    public void deleteTokens(final String username) {
        entityManager.createQuery(DELETE_QUERY.concat("WHERE t.username = :username"))
            .setParameter(QUERY_PARAM_USERNAME, username)
            .executeUpdate();
    }

    @Override
    public void deleteToken(final PasswordlessAuthenticationToken token) {
        val query = DELETE_QUERY.concat(" WHERE t.username = :username AND t.id = :id");
        entityManager.createQuery(query)
            .setParameter(QUERY_PARAM_USERNAME, token.getUsername())
            .setParameter("id", token.getId())
            .executeUpdate();
    }

    @Override
    public PasswordlessAuthenticationToken saveToken(final PasswordlessUserAccount passwordlessAccount,
                                                     final PasswordlessAuthenticationRequest passwordlessRequest,
                                                     final PasswordlessAuthenticationToken authnToken) {
        return FunctionUtils.doUnchecked(() -> {
            val record = JpaPasswordlessAuthenticationEntity.builder()
                .username(authnToken.getUsername())
                .token(encodeToken(authnToken))
                .expirationDate(authnToken.getExpirationDate())
                .build();
            LOGGER.debug("Saving token [{}]", record);
            val result = entityManager.merge(record);
            return authnToken.withId(result.getId());
        });
    }

    @Override
    public void clean() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        LOGGER.debug("Cleaning expired records with an expiration date of [{}]", now);
        val query = DELETE_QUERY.concat(" WHERE t.expirationDate >= :expirationDate");
        entityManager.createQuery(query)
            .setParameter("expirationDate", now)
            .executeUpdate();
    }
}
