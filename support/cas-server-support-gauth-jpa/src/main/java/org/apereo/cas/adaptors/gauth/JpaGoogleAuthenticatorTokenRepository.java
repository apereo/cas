package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.repository.token.BaseGoogleAuthenticatorTokenRepository;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

/**
 * This is {@link JpaGoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerGoogleAuthenticator")
public class JpaGoogleAuthenticatorTokenRepository extends BaseGoogleAuthenticatorTokenRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGoogleAuthenticatorCredentialRepository.class);

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void clean() {
        final int count = this.entityManager.createQuery("DELETE FROM " + GoogleAuthenticatorToken.class.getSimpleName()
                + " r where r.issuedDateTime<= :now", GoogleAuthenticatorToken.class)
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
        logger.debug("Deleted {} expired previously used otp record(s)", count);
    }

    @Override
    public void store(final GoogleAuthenticatorToken token) {
        this.entityManager.merge(token);
    }

    @Override
    public boolean exists(final String uid, final Integer otp) {
        try {
            final GoogleAuthenticatorToken r =
                    this.entityManager.createQuery("SELECT r FROM " + GoogleAuthenticatorToken.class.getSimpleName()
                            + " r where r.userId = :userId and r.token = :token", GoogleAuthenticatorToken.class)
                            .setParameter("userId", uid)
                            .setParameter("token", otp)
                            .getSingleResult();
            return r != null;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id {}", uid);
        }
        return false;
    }
}
