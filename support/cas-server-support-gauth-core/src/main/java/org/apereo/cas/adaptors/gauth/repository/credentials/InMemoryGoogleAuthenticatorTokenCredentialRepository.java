package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.otp.repository.credentials.BaseInMemoryOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InMemoryGoogleAuthenticatorTokenCredentialRepository extends BaseInMemoryOneTimeTokenCredentialRepository {

    private final IGoogleAuthenticator googleAuthenticator;

    /**
     * Instantiates a new In memory google authenticator account registry.
     *
     * @param googleAuthenticator the google authenticator
     */
    public InMemoryGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator) {

        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final GoogleAuthenticatorKey key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
