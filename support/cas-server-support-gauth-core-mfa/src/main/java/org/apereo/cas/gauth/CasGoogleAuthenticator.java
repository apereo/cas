package org.apereo.cas.gauth;

import org.apereo.cas.multitenancy.TenantExtractor;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

/**
 * This is {@link CasGoogleAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface CasGoogleAuthenticator {
    /**
     * Google authenticator bean name.
     */
    String BEAN_NAME = "googleAuthenticatorInstance";

    /**
     * @return secret key
     * @see IGoogleAuthenticator#createCredentials()
     */
    GoogleAuthenticatorKey createCredentials();

    /**
     * @param userName the user name.
     * @return secret key
     * @see IGoogleAuthenticator#createCredentials(String)
     */
    GoogleAuthenticatorKey createCredentials(String userName);


    /**
     * @param secret           the encoded secret key.
     * @param verificationCode the verification code.
     * @return {@code true} if the validation code is valid.
     * @see IGoogleAuthenticator#authorize(String, int) 
     */
    boolean authorize(String secret, int verificationCode);

    /**
     * @return the credential repository used by this instance.
     * @see IGoogleAuthenticator#getCredentialRepository()
     */
    ICredentialRepository getCredentialRepository();

    /**
     * @param repository The credential repository to use, or {@code null} to
     *                   disable this feature.
     * @see IGoogleAuthenticator#setCredentialRepository(ICredentialRepository)
     */
    void setCredentialRepository(ICredentialRepository repository);

    /**
     * @param secret the encoded secret key.
     * @return the current TOTP password.
     * @see IGoogleAuthenticator#getTotpPassword(String)
     */
    int getTotpPassword(String secret);

    /**
     * Gets tenant extractor.
     *
     * @return the tenant extractor
     */
    TenantExtractor getTenantExtractor();
}
