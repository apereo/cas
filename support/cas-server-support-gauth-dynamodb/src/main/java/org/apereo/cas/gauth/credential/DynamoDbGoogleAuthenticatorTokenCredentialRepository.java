package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;

import java.util.Collection;

/**
 * This is {@link DynamoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class DynamoDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator facilitator;

    public DynamoDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                                final CipherExecutor<String, String> tokenCredentialCipher,
                                                                final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator facilitator) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.facilitator = facilitator;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return null;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return null;
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return null;
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        return null;
    }

    @Override
    public void deleteAll() {
    }

    @Override
    public void delete(final String username) {

    }

    @Override
    public void delete(final long id) {

    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long count(final String username) {
        return 0;
    }
}
