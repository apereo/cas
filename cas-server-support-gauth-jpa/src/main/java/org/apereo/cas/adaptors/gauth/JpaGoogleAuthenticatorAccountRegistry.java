package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.ICredentialRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * This is {@link JpaGoogleAuthenticatorAccountRegistry} that stores gauth data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerGoogleAuthenticator")
public class JpaGoogleAuthenticatorAccountRegistry implements ICredentialRepository {

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getSecretKey(final String username) {
        final GoogleAuthenticatorRegistrationRecord r =
                this.entityManager.createQuery("SELECT r FROM GoogleAuthenticatorRegistrationRecord r where r.username = :username",
                        GoogleAuthenticatorRegistrationRecord.class).setParameter("username", username).getSingleResult();
        if (r != null) {
            return r.getSecretKey();
        }
        return null;
    }

    @Override
    public void saveUserCredentials(final String userName, final String secretKey,
                                    final int validationCode,
                                    final List<Integer> scratchCodes) {
        final GoogleAuthenticatorRegistrationRecord r = new GoogleAuthenticatorRegistrationRecord();
        r.setScratchCodes(scratchCodes);
        r.setSecretKey(secretKey);
        r.setUsername(userName);
        r.setValidationCode(validationCode);
        this.entityManager.merge(r);
    }
}
