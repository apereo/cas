package org.jasig.cas.ticket;


public class TicketNotFoundException extends TicketException {
    
    /** The Unique Serializable ID. */
    private static final long serialVersionUID = 3256723974594508849L;

    /** The code description. */
    private static final String CODE = "NOT_FOUND";
    
    public TicketNotFoundException() {
        super(CODE);
    }
    
    public TicketNotFoundException(Throwable throwable) {
        super(CODE, throwable);
    }
}
