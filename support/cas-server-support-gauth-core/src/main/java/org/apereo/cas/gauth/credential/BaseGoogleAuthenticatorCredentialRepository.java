package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

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

    protected BaseGoogleAuthenticatorCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                          final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val account = GoogleAuthenticatorAccount.builder()
            .username(userName)
            .secretKey(secretKey)
            .validationCode(validationCode)
            .scratchCodes(scratchCodes)
            .build();

        update(account);
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        val key = this.googleAuthenticator.createCredentials();
        return GoogleAuthenticatorAccount.builder()
            .username(username)
            .secretKey(key.getKey())
            .validationCode(key.getVerificationCode())
            .scratchCodes(key.getScratchCodes())
            .build();
    }
}
