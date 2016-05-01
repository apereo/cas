package org.jasig.cas.adaptors.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorException;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link GoogleAuthenticatorInstance}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("googleAuthenticatorInstance")
public class GoogleAuthenticatorInstance implements IGoogleAuthenticator {

    @Value("${cas.mfa.gauth.code.digits:6}")
    private int codeDigits;

    @Value("#{${cas.mfa.gauth.time.step:30}*1000}")
    private long timeStepSizeInMillis;

    @Value("${cas.mfa.gauth.window:3}")
    private int windowSize;
    
    private GoogleAuthenticator googleAuthenticator;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder bldr =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        bldr.setCodeDigits(this.codeDigits);
        bldr.setTimeStepSizeInMillis(this.timeStepSizeInMillis);
        bldr.setWindowSize(this.windowSize);
        bldr.setKeyRepresentation(KeyRepresentation.BASE32);

        this.googleAuthenticator = new GoogleAuthenticator(bldr.build());
    }

    public GoogleAuthenticator getGoogleAuthenticator() {
        return this.googleAuthenticator;
    }

    @Override
    public GoogleAuthenticatorKey createCredentials() {
        return this.googleAuthenticator.createCredentials();
    }

    @Override
    public GoogleAuthenticatorKey createCredentials(final String userName) {
        return this.googleAuthenticator.createCredentials(userName);
    }

    @Override
    public boolean authorize(final String secret, final int verificationCode) throws GoogleAuthenticatorException {
        return this.googleAuthenticator.authorize(secret, verificationCode);
    }

    @Override
    public boolean authorizeUser(final String userName, final int verificationCode) throws GoogleAuthenticatorException {
        return this.googleAuthenticator.authorizeUser(userName, verificationCode);
    }
}
