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
     * Wrapper for {@link IGoogleAuthenticator#createCredentials()}.
     * @return secret key
     * @see IGoogleAuthenticator#createCredentials()
     */
    GoogleAuthenticatorKey createCredentials();

    /**
     * Wrapper for {@link IGoogleAuthenticator#createCredentials(String)}.
     * @param userName the user name.
     * @return secret key
     * @see IGoogleAuthenticator#createCredentials(String)
     */
    GoogleAuthenticatorKey createCredentials(String userName);


    /**
     * Wrapper for {@link IGoogleAuthenticator#authorize(String, int)}.
     * @param secret           the encoded secret key.
     * @param verificationCode the verification code.
     * @return {@code true} if the validation code is valid.
     * @see IGoogleAuthenticator#authorize(String, int) 
     */
    boolean authorize(String secret, int verificationCode);

    /**
     * Wrapper for {@link IGoogleAuthenticator#getCredentialRepository()}.
     * @return the credential repository used by this instance.
     * @see IGoogleAuthenticator#getCredentialRepository()
     */
    ICredentialRepository getCredentialRepository();

    /**
     * Sets the credential repository to use.
     * @param repository The credential repository to use, or {@code null} to
     *                   disable this feature.
     * @see IGoogleAuthenticator#setCredentialRepository(ICredentialRepository)
     */
    void setCredentialRepository(ICredentialRepository repository);

    /**
     * Wrapper for {@link IGoogleAuthenticator#getTotpPassword(String)}.
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
