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
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link JpaGoogleAuthenticatorTokenCredentialRepository} that stores gauth data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement
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
    public OneTimeTokenAccount get(final long id) {
        return entityManager.find(JpaGoogleAuthenticatorAccount.class, id);
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return entityManager.createQuery("SELECT r FROM "
            + ENTITY_NAME + " r WHERE r.id=:id AND r.username = :username", JpaGoogleAuthenticatorAccount.class)
            .setParameter("username", username.toLowerCase().trim())
            .setParameter("id", id)
            .getSingleResult();
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val accounts = fetchAccounts(username);
        accounts.forEach(entityManager::detach);
        return decode(accounts);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val results = entityManager.createQuery("SELECT r FROM "
            + ENTITY_NAME + " r", JpaGoogleAuthenticatorAccount.class).getResultList();
        return results.stream()
            .map(account -> {
                entityManager.detach(account);
                return decode(account);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        val ac = JpaGoogleAuthenticatorAccount.from(account);
        val encoded = encode(ac);
        return entityManager.merge(encoded);
    }

    @Override
    @SneakyThrows
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val ac = entityManager.find(JpaGoogleAuthenticatorAccount.class, account.getId());
        if (ac != null) {
            ac.setValidationCode(account.getValidationCode());
            ac.setScratchCodes(account.getScratchCodes());
            ac.setSecretKey(account.getSecretKey());
            val encoded = encode(ac);
            return entityManager.merge(encoded);
        }
        return null;
    }

    @Override
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM " + OneTimeTokenAccount.TABLE_NAME_SCRATCH_CODES).executeUpdate();
        entityManager.createQuery("DELETE FROM " + ENTITY_NAME).executeUpdate();
    }

    @Override
    public void delete(final String username) {
        val acct = fetchAccounts(username);
        acct.forEach(entityManager::remove);
        LOGGER.debug("Deleted account record for [{}]", username);
    }

    @Override
    public void delete(final long id) {
        entityManager.createNativeQuery("DELETE FROM " + OneTimeTokenAccount.TABLE_NAME_SCRATCH_CODES + " WHERE id = :id")
            .setParameter("id", id)
            .executeUpdate();

        entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE r.id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    public long count() {
        val count = (Number) entityManager.createQuery("SELECT COUNT(r.username) FROM " + ENTITY_NAME + " r").getSingleResult();
        LOGGER.debug("Counted [{}] record(s)", count);
        return count.longValue();
    }

    @Override
    public long count(final String username) {
        val count = (Number) entityManager.createQuery(
            "SELECT COUNT(r.username) FROM " + ENTITY_NAME + " r WHERE r.username=:username")
            .setParameter("username", username.toLowerCase().trim())
            .getSingleResult();
        LOGGER.debug("Counted [{}] record(s) for [{}]", count, username);
        return count.longValue();
    }

    private List<JpaGoogleAuthenticatorAccount> fetchAccounts(final String username) {
        return entityManager.createQuery("SELECT r FROM "
            + ENTITY_NAME + " r WHERE r.username = :username", JpaGoogleAuthenticatorAccount.class)
            .setParameter("username", username.toLowerCase().trim())
            .getResultList();
    }
}
