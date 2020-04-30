package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
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
@Getter
public class JpaGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private static final String ENTITY_NAME = JpaGoogleAuthenticatorAccount.class.getSimpleName();

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                           final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher, googleAuthenticator);
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            val account = fetchAccount(username);
            this.entityManager.detach(account);
            return decode(account);
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    private JpaGoogleAuthenticatorAccount fetchAccount(final String username) {
        return this.entityManager.createQuery("SELECT r FROM "
            + ENTITY_NAME + " r where r.username = :username", JpaGoogleAuthenticatorAccount.class)
            .setParameter("username", username)
            .getSingleResult();
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        try {
            val r = this.entityManager.createQuery("SELECT r FROM "
                + ENTITY_NAME + " r", JpaGoogleAuthenticatorAccount.class).getResultList();
            val results = new ArrayList<OneTimeTokenAccount>(r.size());
            r.forEach(account -> {
                this.entityManager.detach(account);
                results.add(decode(account));
            });
            return results;
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val r = new JpaGoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(r);
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
        this.entityManager.createNativeQuery("DELETE FROM " + OneTimeTokenAccount.TABLE_NAME_SCRATCH_CODES).executeUpdate();
        this.entityManager.createQuery("DELETE FROM " + ENTITY_NAME).executeUpdate();
    }

    @Override
    public void delete(final String username) {
        val acct = fetchAccount(username);
        this.entityManager.remove(acct);
        LOGGER.debug("Deleted account record for [{}]", username);
    }

    @Override
    public long count() {
        val count = (Number) this.entityManager.createQuery("SELECT COUNT(r.username) FROM " + ENTITY_NAME + " r").getSingleResult();
        LOGGER.debug("Counted [{}] record(s)", count);
        return count.longValue();
    }
}
