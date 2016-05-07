package org.apereo.cas.ticket;

/**
 * AbstractTicketException to alert that a Ticket was not found or that it is expired.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class InvalidTicketException extends AbstractTicketException {

    private static final long serialVersionUID = 9141891414482490L;

    /** The code description. */
    private static final String CODE = "INVALID_TICKET";

    private final String ticketId;

    /**
     * Constructs a InvalidTicketException with the default exception code.
     * @param ticketId the ticket id that originally caused this exception to be thrown.
     */
    public InvalidTicketException(final String ticketId) {
        super(CODE);
        this.ticketId = ticketId;
    }

    /**
     * Constructs a InvalidTicketException with the default exception code and
     * the original exception that was thrown.
     *
     * @param throwable the chained exception
     * @param ticketId the ticket id that originally caused this exception to be thrown.
     */
    public InvalidTicketException(final Throwable throwable, final String ticketId) {
        super(CODE, throwable);
        this.ticketId = ticketId;
    }

    /**
     * Returns the ticket id that causes this exception.
     * @return the ticket id
     * @see InvalidTicketException#ticketId
     */
    @Override
    public String getMessage() {
        return this.ticketId;
    }
}
