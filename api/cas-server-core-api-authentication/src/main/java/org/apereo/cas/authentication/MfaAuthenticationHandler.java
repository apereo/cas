package org.apereo.cas.authentication;

/**
 * Interface implemented by AuthenticationHandlers used to provide Mfa authentication.
 *
 * @author Travis schmidt
 * @since  5.3.4
 */
public interface MfaAuthenticationHandler {

    /**
     * Method returns the provider id of the configured provider used by this AuthenticationHandler.
     * @return - the provider id
     */
    String getProviderId();

    /**
     * Method to determine if this handler is able to authenticate the passed credential if it was
     * created by the same provider that this handler uses to authenticate.
     *
     * @param credential - the credential
     * @return - true if supported.
     */
    default boolean supports(final MfaCredential credential) {
        return getProviderId().equals(credential.getProviderId());
    }
}
