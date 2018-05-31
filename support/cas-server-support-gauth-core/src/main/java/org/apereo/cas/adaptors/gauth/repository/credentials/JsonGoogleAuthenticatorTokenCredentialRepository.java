package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.otp.repository.credentials.BaseJsonOneTimeTokenCredentialRepository;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.springframework.core.io.Resource;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class JsonGoogleAuthenticatorTokenCredentialRepository extends BaseJsonOneTimeTokenCredentialRepository {
    private final IGoogleAuthenticator googleAuthenticator;

    public JsonGoogleAuthenticatorTokenCredentialRepository(final Resource location, final IGoogleAuthenticator googleAuthenticator,
                                                            final CipherExecutor<String, String> tokenCredentialCipher) {
        super(location, tokenCredentialCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final var key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }

}
