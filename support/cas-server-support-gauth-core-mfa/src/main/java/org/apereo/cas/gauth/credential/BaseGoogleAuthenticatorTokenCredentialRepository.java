package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;

/**
 * This is {@link BaseGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
public abstract class BaseGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {

    /**
     * Google authenticator token creator.
     */
    protected final IGoogleAuthenticator googleAuthenticator;

    public BaseGoogleAuthenticatorTokenCredentialRepository(
        final CipherExecutor<String, String> tokenCredentialCipher,
        final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    /**
     * Create one time token account.
     *
     * @param username the username
     * @return the one time token account
     */
    @Override
    public OneTimeTokenAccount create(final String username) {
        val key = getGoogleAuthenticator().createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
