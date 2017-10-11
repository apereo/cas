package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * This is {@link JpaGoogleAuthenticatorTokenCredentialRepository} that stores gauth data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerGoogleAuthenticator")
public class JpaGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGoogleAuthenticatorTokenCredentialRepository.class);

    private final IGoogleAuthenticator googleAuthenticator;

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private EntityManager entityManager;

    public JpaGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator) {
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            final GoogleAuthenticatorAccount r =
                    this.entityManager.createQuery("SELECT r FROM " + GoogleAuthenticatorAccount.class.getSimpleName()
                                    + " r where r.username = :username",
                            GoogleAuthenticatorAccount.class).setParameter("username", username).getSingleResult();
            return r;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        final GoogleAuthenticatorAccount r = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        this.entityManager.merge(r);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final GoogleAuthenticatorKey key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
