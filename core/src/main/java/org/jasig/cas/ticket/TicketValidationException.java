package org.jasig.cas.ticket;


public class TicketValidationException extends TicketException {

    /** Unique Serial ID. */
    private static final long serialVersionUID = 3257004341537093175L;

    /** The code description. */
    private static final String CODE = "VALIDATION_ERROR";
    
    public TicketValidationException() {
        super(CODE);
    }
    
    public TicketValidationException(Throwable throwable) {
        super(CODE, throwable);
    }
    
}
