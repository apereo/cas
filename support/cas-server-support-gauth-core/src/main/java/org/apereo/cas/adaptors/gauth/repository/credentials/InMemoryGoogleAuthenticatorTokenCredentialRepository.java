package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.otp.repository.credentials.BaseInMemoryOneTimeTokenCredentialRepository;
import org.apereo.cas.authentication.OneTimeTokenAccount;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class InMemoryGoogleAuthenticatorTokenCredentialRepository extends BaseInMemoryOneTimeTokenCredentialRepository {
    private final IGoogleAuthenticator googleAuthenticator;

    public InMemoryGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                                final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final var key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
