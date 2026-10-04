package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * This is {@link JpaGoogleAuthenticatorTokenCredentialRepository} that stores gauth data into a RDBMS database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerGoogleAuthenticator")
@Slf4j
@ToString
@Getter
public class JpaGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private static final String ENTITY_NAME = JpaGoogleAuthenticatorAccount.class.getSimpleName();

    @PersistenceContext(unitName = "jpaGoogleAuthenticatorContext")
    private EntityManager entityManager;

    public JpaGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                           final CipherExecutor<Number, Number> scratchCodesCipher,
                                                           final CasGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
    }

    /**
     * Find the account by id and return a decoded, detached copy, like every other read here.
     * Handing out the managed entity would expose the encoded secret and scratch codes, and a caller that
     * passes it back to {@link #update(OneTimeTokenAccount)} would have them encoded a second time.
     *
     * @param id the account id
     * @return the decoded account, or null when there is none
     */
    @Override
    public @Nullable OneTimeTokenAccount get(final long id) {
        return Optional.ofNullable(entityManager.find(JpaGoogleAuthenticatorAccount.class, id))
            .map(this::detachAndDecode)
            .orElse(null);
    }

    /**
     * Find the account by owner and id and return a decoded, detached copy, or null when the user has no
     * such account.
     *
     * @param username the owner
     * @param id       the account id
     * @return the decoded account, or null when there is none
     */
    @Override
    public @Nullable OneTimeTokenAccount get(final String username, final long id) {
        return entityManager.createQuery("SELECT r FROM "
                + ENTITY_NAME + " r WHERE r.id=:id AND r.username = :username", JpaGoogleAuthenticatorAccount.class)
            .setParameter("username", username.toLowerCase(Locale.ENGLISH).trim())
            .setParameter("id", id)
            .getResultList()
            .stream()
            .findFirst()
            .map(this::detachAndDecode)
            .orElse(null);
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

    /**
     * Save the account, replacing the stored account with the same id. An account whose id is not in the table, such as
     * one imported from another repository, is inserted and gets an id from the database, since merging an entity whose
     * generated id is not in the table fails.
     *
     * @param account the account
     * @return the encoded account as stored
     */
    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        val stored = account.getId() > 0 && entityManager.find(JpaGoogleAuthenticatorAccount.class, account.getId()) != null;
        return insertOrReplace(account, stored);
    }

    /**
     * Update the stored account with the same id, or insert the account when none is stored,
     * so that every backend treats an update of a missing device as an insert. The database assigns
     * the id of an inserted device, as in {@link #save(OneTimeTokenAccount)}.
     *
     * @param account the account
     * @return the encoded account as stored
     */
    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val ac = entityManager.find(JpaGoogleAuthenticatorAccount.class, account.getId());
        if (ac == null) {
            return insertOrReplace(account, false);
        }
        val encoded = encode(account);
        ac.setValidationCode(encoded.getValidationCode());
        ac.setScratchCodes(encoded.getScratchCodes()
            .stream()
            .map(code -> new BigInteger(code.toString()))
            .collect(Collectors.toList()));
        ac.setSecretKey(encoded.getSecretKey());
        ac.setProperties(new ArrayList<>(encoded.getProperties()));
        ac.setLastUsedDateTime(encoded.getLastUsedDateTime());
        return entityManager.merge(ac);
    }

    @Override
    public void deleteAll() {
        entityManager.createNativeQuery("DELETE FROM " + OneTimeTokenAccount.TABLE_NAME_SCRATCH_CODES).executeUpdate();
        entityManager.createQuery("DELETE FROM " + ENTITY_NAME).executeUpdate();
    }

    /**
     * Delete the user's accounts with one bulk delete rather than loading and removing them one by one.
     * Hibernate also deletes the rows of the accounts' collection tables, such as scratch codes and properties.
     *
     * @param username the username
     */
    @Override
    public void delete(final String username) {
        val count = entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE r.username = :username")
            .setParameter("username", username.toLowerCase(Locale.ENGLISH).trim())
            .executeUpdate();
        LOGGER.debug("Deleted [{}] account record(s) for [{}]", count, username);
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
            .setParameter("username", username.toLowerCase(Locale.ENGLISH).trim())
            .getSingleResult();
        LOGGER.debug("Counted [{}] record(s) for [{}]", count, username);
        return count.longValue();
    }

    private OneTimeTokenAccount insertOrReplace(final OneTimeTokenAccount account, final boolean stored) {
        val entity = JpaGoogleAuthenticatorAccount.from(account);
        if (!stored) {
            entity.setId(0);
        }
        return entityManager.merge(encode(entity));
    }

    private OneTimeTokenAccount detachAndDecode(final JpaGoogleAuthenticatorAccount account) {
        entityManager.detach(account);
        return decode(account);
    }

    private List<JpaGoogleAuthenticatorAccount> fetchAccounts(final String username) {
        return entityManager.createQuery("SELECT r FROM "
                + ENTITY_NAME + " r WHERE r.username = :username", JpaGoogleAuthenticatorAccount.class)
            .setParameter("username", username.toLowerCase(Locale.ENGLISH).trim())
            .getResultList();
    }
}
