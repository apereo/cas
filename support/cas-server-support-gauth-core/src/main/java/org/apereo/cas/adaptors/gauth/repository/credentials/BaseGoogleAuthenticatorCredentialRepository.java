package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;

import java.util.List;

/**
 * This is {@link BaseGoogleAuthenticatorCredentialRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseGoogleAuthenticatorCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    /**
     * Google authenticator instance.
     */
    protected IGoogleAuthenticator googleAuthenticator;

    public BaseGoogleAuthenticatorCredentialRepository(final IGoogleAuthenticator googleAuthenticator, final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        val key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
