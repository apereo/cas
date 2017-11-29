package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

/**
 * An exception that may be thrown during service ticket validation
 * to indicate that the service ticket is not valid for the authentication
 * context that is requested.
 * @author Misagh Moayyed
 * @since 4.3
 */
public class UnsatisfiedAuthenticationContextTicketValidationException extends AbstractTicketValidationException {
    /** The code description. */
    protected static final String CODE = "INVALID_AUTHENTICATION_CONTEXT";

    private static final long serialVersionUID = -8076771862820008358L;

    /**
     * Instantiates a new Unrecognizable service for service ticket validation exception.
     *
     * @param service the service
     */
    public UnsatisfiedAuthenticationContextTicketValidationException(final Service service) {
        super(CODE, service);
    }
}
