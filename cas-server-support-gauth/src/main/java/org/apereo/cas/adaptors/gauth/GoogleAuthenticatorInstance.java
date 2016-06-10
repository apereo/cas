package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorException;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleAuthenticatorInstance}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorInstance implements IGoogleAuthenticator {

    @Autowired
    private CasConfigurationProperties casProperties;

    private GoogleAuthenticator googleAuthenticator;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder bldr =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        bldr.setCodeDigits(casProperties.getAuthn().getMfa().getGauth().getCodeDigits());
        bldr.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(casProperties.getAuthn().getMfa().getGauth().getTimeStepSize()));
        bldr.setWindowSize(casProperties.getAuthn().getMfa().getGauth().getWindowSize());
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

    @Override
    public boolean authorize(final String secret, final int verificationCode, final long time) throws GoogleAuthenticatorException {
        return this.googleAuthenticator.authorize(secret, verificationCode, time);
    }

    @Override
    public boolean authorizeUser(final String userName, final int verificationCode, final long time) throws GoogleAuthenticatorException {
        return this.googleAuthenticator.authorize(userName, verificationCode, time);
    }
}
