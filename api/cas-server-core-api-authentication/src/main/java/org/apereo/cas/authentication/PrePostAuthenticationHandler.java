package org.apereo.cas.authentication;

/**
 * This is {@link PrePostAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface PrePostAuthenticationHandler extends AuthenticationHandler {

    /**
     * Template method to perform arbitrary pre-authentication actions.
     *
     * @param credential the Credential supplied
     * @return true if authentication should continue, false otherwise.
     */
    default boolean preAuthenticate(final Credential credential) {
        return true;
    }

    /**
     * Template method to perform arbitrary post-authentication actions.
     *
     * @param credential the supplied credential
     * @param result     the result of the authentication attempt.
     * @return An authentication handler result that MAY be different or modified from that provided.
     */
    default HandlerResult postAuthenticate(final Credential credential, final HandlerResult result) {
        return result;
    }
}
