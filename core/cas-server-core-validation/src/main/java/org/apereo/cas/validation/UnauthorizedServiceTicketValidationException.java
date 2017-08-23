package org.apereo.cas.validation;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicketValidationException;

/**
 * An exception that may be thrown during service ticket validation
 * to indicate that the event is not authorized.
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class UnauthorizedServiceTicketValidationException extends AbstractTicketValidationException {
    /** The code description. */
    protected static final String CODE = "INVALID_SERVICE";

    private static final long serialVersionUID = -8076771862820008358L;
    
    public UnauthorizedServiceTicketValidationException(final Service service) {
        super(CODE, service);
    }
}
