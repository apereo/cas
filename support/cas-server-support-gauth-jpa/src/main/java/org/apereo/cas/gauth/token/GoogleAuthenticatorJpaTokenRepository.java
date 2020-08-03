package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link GoogleAuthenticatorJpaTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerGoogleAuthenticator")
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorJpaTokenRepository extends BaseOneTimeTokenRepository {
    private final long expireTokensInSeconds;

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public void cleanInternal() {
        val count = this.entityManager.createQuery("DELETE FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName()
            + " r WHERE r.issuedDateTime>= :expired")
            .setParameter("expired", LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(this.expireTokensInSeconds))
            .executeUpdate();
        LOGGER.debug("Deleted [{}] expired previously used token record(s)", count);
    }

    @SneakyThrows
    @Override
    public void store(final OneTimeToken token) {
        val gToken = new JpaGoogleAuthenticatorToken();
        BeanUtils.copyProperties(gToken, token);
        this.entityManager.merge(gToken);
    }

    @Override
    public GoogleAuthenticatorToken get(final String uid, final Integer otp) {
        try {
            return this.entityManager.createQuery("SELECT r FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName()
                + " r WHERE r.userId = :userId and r.token = :token", JpaGoogleAuthenticatorToken.class)
                .setParameter("userId", uid)
                .setParameter("token", otp)
                .getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", uid);
        }
        return null;
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        val count = this.entityManager.createQuery("DELETE FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName()
            + " r WHERE r.userId = :userId and r.token = :token")
            .setParameter("userId", uid)
            .setParameter("token", otp)
            .executeUpdate();
        LOGGER.debug("Deleted [{}] token record(s)", count);
    }

    @Override
    public void remove(final String uid) {
        val count = this.entityManager.createQuery("DELETE FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName() + " r WHERE r.userId= :userId")
            .setParameter("userId", uid)
            .executeUpdate();
        LOGGER.debug("Deleted [{}] token record(s)", count);
    }

    @Override
    public void remove(final Integer otp) {
        val count = this.entityManager.createQuery("DELETE FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName() + " r WHERE r.token= :token")
            .setParameter("token", otp)
            .executeUpdate();
        LOGGER.debug("Deleted [{}] token record(s)", count);
    }

    @Override
    public void removeAll() {
        this.entityManager.createQuery("DELETE FROM "
            + JpaGoogleAuthenticatorToken.class.getSimpleName() + " r").executeUpdate();
    }

    @Override
    public long count(final String uid) {
        val count = (Number) this.entityManager.createQuery("SELECT COUNT(r.userId) FROM "
            + JpaGoogleAuthenticatorToken.class.getSimpleName() + " r WHERE r.userId= :userId")
            .setParameter("userId", uid)
            .getSingleResult();
        LOGGER.debug("Counted [{}] token record(s) for [{}]", count, uid);
        return count.longValue();
    }

    @Override
    public long count() {
        val count = (Number) this.entityManager.createQuery("SELECT COUNT(r.userId) FROM " + JpaGoogleAuthenticatorToken.class.getSimpleName() + " r").getSingleResult();
        LOGGER.debug("Counted [{}] token record(s)", count);
        return count.longValue();
    }
}
