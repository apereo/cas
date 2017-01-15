package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.otp.repository.credentials.BaseJsonOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.springframework.core.io.Resource;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JsonGoogleAuthenticatorTokenCredentialRepository extends BaseJsonOneTimeTokenCredentialRepository {
    private final IGoogleAuthenticator googleAuthenticator;

    public JsonGoogleAuthenticatorTokenCredentialRepository(final Resource location, final IGoogleAuthenticator googleAuthenticator) {
        super(location);
        this.googleAuthenticator = googleAuthenticator;
    }
    
    @Override
    public OneTimeTokenAccount create(final String username) {
        final GoogleAuthenticatorKey key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }

}
