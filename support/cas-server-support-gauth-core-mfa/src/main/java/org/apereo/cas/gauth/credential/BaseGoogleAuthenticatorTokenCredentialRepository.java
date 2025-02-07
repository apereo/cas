package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This is {@link BaseGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
public abstract class BaseGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "googleAuthenticatorAccountRegistry";

    /**
     * Google authenticator token creator.
     */
    protected final IGoogleAuthenticator googleAuthenticator;

    protected BaseGoogleAuthenticatorTokenCredentialRepository(
        final CipherExecutor<String, String> tokenCredentialCipher,
        final CipherExecutor<Number, Number> scratchCodesCipher,
        final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher, scratchCodesCipher);
        this.googleAuthenticator = googleAuthenticator;
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        val key = getGoogleAuthenticator().createCredentials();
        return GoogleAuthenticatorAccount.builder()
            .username(username)
            .secretKey(key.getKey())
            .validationCode(key.getVerificationCode())
            .scratchCodes(new ArrayList<>(key.getScratchCodes()))
            .name(UUID.randomUUID().toString())
            .build();
    }
}
