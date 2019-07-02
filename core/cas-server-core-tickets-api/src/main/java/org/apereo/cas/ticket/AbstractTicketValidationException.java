package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;

import java.util.List;

/**
 * Exception to alert that there was an error validating the ticket.
 *
 * @author Scott Battaglia
 * @since 4.2.0
 */
@Getter
public abstract class AbstractTicketValidationException extends AbstractTicketException {
    /**
     * The code description.
     */
    protected static final String CODE = "INVALID_TICKET";

    private static final long serialVersionUID = 3257004341537093175L;

    private final Service service;

    /**
     * Constructs a AbstractTicketValidationException with the default exception code
     * and the original exception that was thrown.
     *
     * @param service original service
     */
    public AbstractTicketValidationException(final Service service) {
        this(CODE, service);
    }

    public AbstractTicketValidationException(final String code, final Service service) {
        super(code);
        this.service = service;
    }

    public AbstractTicketValidationException(final String code, final String msg,
                                             final List<Object> args, final Service service) {
        super(code, msg, args);
        this.service = service;
    }

    public AbstractTicketValidationException(final String code, final Throwable throwable,
                                             final List<Object> args, final Service service) {
        super(code, throwable, args);
        this.service = service;
    }
}
