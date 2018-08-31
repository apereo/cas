package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link JpaGoogleAuthenticatorTokenCredentialRepository} that stores gauth data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerGoogleAuthenticator")
@Slf4j
@ToString
public class JpaGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    private static final String ENTITY_NAME = GoogleAuthenticatorAccount.class.getSimpleName();

    private final IGoogleAuthenticator googleAuthenticator;

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                           final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            val r = this.entityManager.createQuery("SELECT r FROM " + ENTITY_NAME + " r where r.username = :username",
                GoogleAuthenticatorAccount.class)
                .setParameter("username", username)
                .getSingleResult();
            this.entityManager.detach(r);
            return decode(r);
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        try {
            val results = new ArrayList<OneTimeTokenAccount>();
            val r = this.entityManager.createQuery("SELECT r FROM " + ENTITY_NAME + " r", GoogleAuthenticatorAccount.class).getResultList();
            r.forEach(account -> {
                this.entityManager.detach(account);
                results.add(decode(account));
            });
            return results;
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val r = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(r);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        val key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }

    @Override
    @SneakyThrows
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val ac = get(account.getUsername());
        if (ac != null) {
            ac.setValidationCode(account.getValidationCode());
            ac.setScratchCodes(account.getScratchCodes());
            ac.setSecretKey(account.getSecretKey());
            val encoded = encode(ac);
            this.entityManager.merge(encoded);
            return encoded;
        }
        val encoded = encode(account);
        this.entityManager.merge(encoded);
        return encoded;
    }

    @Override
    public void deleteAll() {
        this.entityManager.createQuery("DELETE FROM " + ENTITY_NAME).executeUpdate();
    }

    @Override
    public void delete(final String username) {
        val count = this.entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE r.username= :username")
            .setParameter("username", username)
            .executeUpdate();
        LOGGER.debug("Deleted [{}] record(s)", count);
    }

    @Override
    public long count() {
        val count = (Number) this.entityManager.createQuery("SELECT COUNT(r.username) FROM " + ENTITY_NAME + " r").getSingleResult();
        LOGGER.debug("Counted [{}] record(s)", count);
        return count.longValue();
    }
}
