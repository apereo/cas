package org.apereo.cas.ticket;

import lombok.Getter;

/**
 * AbstractTicketException to alert that a Ticket was not found or that it is expired.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Getter
public class InvalidTicketException extends AbstractTicketException {

    private static final long serialVersionUID = 9141891414482490L;

    /**
     * The code description.
     */
    private static final String CODE = "INVALID_TICKET";

    private final String ticketId;

    public InvalidTicketException(final String ticketId) {
        super(CODE);
        this.ticketId = ticketId;
    }

    public InvalidTicketException(final Throwable throwable, final String ticketId) {
        super(CODE, throwable);
        this.ticketId = ticketId;
    }
}
