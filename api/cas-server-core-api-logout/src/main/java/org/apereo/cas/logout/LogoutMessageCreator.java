package org.apereo.cas.logout;

/**
 * Contract that defines the format of the logout message sent to a client to indicate
 * that an SSO session has terminated.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@FunctionalInterface
public interface LogoutMessageCreator {
    /**
     * Builds the logout message to be sent.
     *
     * @param request the request
     * @return the message. Message may or may not be encoded.
     */
    String create(LogoutRequest request);
}
