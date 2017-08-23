package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

/**
 * Exception thrown when a ST has already granted a PGT and is asked to do so again.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InvalidProxyGrantingTicketForServiceTicketException extends AbstractTicketValidationException {
    private static final long serialVersionUID = 2120177571513373134L;

    private static final String CODE = "INVALID_PROXY_GRANTING_TICKET";

    /**
     * Instantiates a new Invalid proxy granting ticket for service ticket.
     *
     * @param service the service
     */
    public InvalidProxyGrantingTicketForServiceTicketException(final Service service) {
        this(CODE, service);
    }

    /**
     * Instantiates a new Invalid proxy granting ticket for service ticket.
     *
     * @param code    the code
     * @param service the service
     */
    public InvalidProxyGrantingTicketForServiceTicketException(final String code, final Service service) {
        super(code, service);
    }
}
