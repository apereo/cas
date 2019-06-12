package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

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
@Endpoint(id = "gauthCredentialRepository", enableByDefault = false)
public class GoogleAuthenticatorTokenCredentialRepositoryEndpoint extends BaseCasActuatorEndpoint {
    private final OneTimeTokenCredentialRepository repository;

    public GoogleAuthenticatorTokenCredentialRepositoryEndpoint(final CasConfigurationProperties casProperties,
                                                                final OneTimeTokenCredentialRepository repository) {
        super(casProperties);
        this.repository = repository;
    }

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
