package org.jasig.cas.ticket;

/**
 * Exception thrown if there is an error creating a ticket.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class TicketCreationException extends AbstractTicketException {

    /** Serializable ID for unique id. */
    private static final long serialVersionUID = 5501212207531289993L;

    /** Code description. */
    private static final String CODE = "CREATION_ERROR";

    /**
     * Constructs a TicketCreationException with the default exception code.
     */
    public TicketCreationException() {
        super(CODE);
    }

    /**
     * Constructs a TicketCreationException with the default exception code and
     * the original exception that was thrown.
     *
     * @param throwable the chained exception
     */
    public TicketCreationException(final Throwable throwable) {
        super(CODE, throwable);
    }
}
