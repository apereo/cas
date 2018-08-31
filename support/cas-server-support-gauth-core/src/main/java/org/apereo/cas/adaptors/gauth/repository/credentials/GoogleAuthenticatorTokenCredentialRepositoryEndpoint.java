package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Collection;

/**
 * This is {@link GoogleAuthenticatorTokenCredentialRepositoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Endpoint(id = "gauth-credential-repository", enableByDefault = false)
public class GoogleAuthenticatorTokenCredentialRepositoryEndpoint {
    private final OneTimeTokenCredentialRepository repository;

    /**
     * Get one time token account.
     *
     * @param username the username
     * @return the one time token account
     */
    @ReadOperation
    public OneTimeTokenAccount get(@Selector final String username) {
        return repository.get(username);
    }

    /**
     * Load collection.
     *
     * @return the collection
     */
    @ReadOperation
    public Collection<? extends OneTimeTokenAccount> load() {
        return repository.load();
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteOperation
    public void delete(@Selector final String username) {
        repository.delete(username);
    }

    /**
     * Delete all.
     */
    @DeleteOperation
    public void deleteAll() {
        repository.deleteAll();
    }
}
