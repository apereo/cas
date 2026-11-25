package org.apereo.cas.otp.repository.credentials;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OneTimeTokenCredentialRepository}.
 * The repository holds the accounts of every tenant; the {@code ForCurrentTenant} lookups return only the accounts
 * that belong to the tenant of the current request, and are the ones to use when acting for a user.
 * An account registered without a tenant belongs only to requests outside of any tenant.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OneTimeTokenCredentialRepository {
    /**
     * Get one time token account.
     *
     * @param id the id
     * @return the one time token account
     */
    OneTimeTokenAccount get(long id);

    /**
     * Get one time token account.
     *
     * @param username the username
     * @param id       the id
     * @return the one time token account
     */
    OneTimeTokenAccount get(String username, long id);

    /**
     * Gets accounts.
     *
     * @param username the username
     * @return the secret
     */
    Collection<? extends OneTimeTokenAccount> get(String username);

    /**
     * Load all accounts into collection.
     *
     * @return the collection
     */
    Collection<? extends OneTimeTokenAccount> load();

    /**
     * Save.
     *
     * @param account the account
     * @return one time token account
     */
    OneTimeTokenAccount save(OneTimeTokenAccount account);

    /**
     * Create one time token account.
     *
     * @param username the username
     * @return the one time token account
     */
    OneTimeTokenAccount create(String username);

    /**
     * Update an existing account.
     *
     * @param account the registration record
     * @return the one time token account
     */
    OneTimeTokenAccount update(OneTimeTokenAccount account);

    /**
     * Delete all tokens in the repository.
     */
    void deleteAll();

    /**
     * Delete.
     *
     * @param username the username
     */
    void delete(String username);

    /**
     * Delete by id.
     *
     * @param id device identifier
     */
    void delete(long id);

    /**
     * Count all records.
     *
     * @return the long
     */
    long count();

    /**
     * Count records for username.
     *
     * @param username the username
     * @return the count
     */
    long count(String username);

    /**
     * The accounts of the user that belong to the tenant of the current request.
     *
     * @param username the username
     * @return the accounts, or an empty list
     */
    default List<OneTimeTokenAccount> getForCurrentTenant(final String username) {
        val tenant = currentTenant();
        return Optional.ofNullable(get(username))
            .stream()
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .filter(account -> account.isAssignedToTenant(tenant))
            .map(OneTimeTokenAccount.class::cast)
            .toList();
    }

    /**
     * The account of the user with the given id, when it belongs to the tenant of the current request.
     *
     * @param username the username
     * @param id       the account id
     * @return the account, or null
     */
    default @Nullable OneTimeTokenAccount getForCurrentTenant(final String username, final long id) {
        return Optional.ofNullable(get(username, id))
            .filter(account -> account.isAssignedToTenant(currentTenant()))
            .orElse(null);
    }

    /**
     * Count the accounts of the user that belong to the tenant of the current request.
     *
     * @param username the username
     * @return the count
     */
    default long countForCurrentTenant(final String username) {
        return getForCurrentTenant(username).size();
    }

    private static @Nullable String currentTenant() {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo()).map(ClientInfo::getTenant).orElse(null);
    }
}
