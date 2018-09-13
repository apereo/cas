package org.apereo.cas.authentication;

/**
 * Interface implemented by AuthenticationHandlers that are used to authenticate credentials
 * that are created by MFA providers that can configure multiple instances in the CAS server.
 * The id of the MFA provider instance is used to determine if the instance of the
 * AuthenticationHandler can authenticate the proposed credential.
 *
 * @author Travis schmidt
 * @since  5.3.4
 */
public interface MultiInstanceMfaAuthenticationHandler {

    /**
     * Method returns the provider id of the configured provider used by this AuthenticationHandler.
     * @return - the provider id
     */
    String getMultifactorProviderId();

    /**
     * Method to determine if this handler is able to authenticate the passed credential if it was
     * created by the same provider that this handler uses to authenticate.
     *
     * @param credential - the credential
     * @return - true if supported.
     */
    default boolean supports(final MultiInstanceMfaCredential credential) {
        return getMultifactorProviderId().equals(credential.getMultifactorProviderId());
    }
}
