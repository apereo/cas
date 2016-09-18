package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

/**
 * Exception to alert that there was an error validating the ticket.
 *
 * @author Scott Battaglia
 * @since 4.2.0
 */
public abstract class AbstractTicketValidationException extends AbstractTicketException {
    /** The code description. */
    protected static final String CODE = "INVALID_TICKET";

    /** Unique Serial ID. */
    private static final long serialVersionUID = 3257004341537093175L;

    private final Service service;

    /**
     * Constructs a AbstractTicketValidationException with the default exception code
     * and the original exception that was thrown.
     * @param service original service
     */
    public AbstractTicketValidationException(final Service service) {
        this(CODE, service);
    }

    /**
     * Instantiates a new Ticket validation exception.
     *
     * @param code the code
     * @param service the service
     * @since 4.1
     */
    public AbstractTicketValidationException(final String code, final Service service) {
        super(code);
        this.service = service;
    }

    public Service getOriginalService() {
        return this.service;
    }

}
