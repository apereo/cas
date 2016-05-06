package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

/**
 * An exception that may be thrown during service ticket validation
 * to indicate that the service ticket is not valid and was not originally
 * issued for the submitted service.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class UnrecognizableServiceForServiceTicketValidationException extends AbstractTicketValidationException {
    /** The code description. */
    protected static final String CODE = "INVALID_SERVICE";

    private static final long serialVersionUID = -8076771862820008358L;

    /**
     * Instantiates a new Unrecognizable service for service ticket validation exception.
     *
     * @param service the service
     */
    public UnrecognizableServiceForServiceTicketValidationException(final Service service) {
        super(CODE, service);
    }
}
