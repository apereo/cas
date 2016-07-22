package org.apereo.cas.ticket;

import org.apereo.cas.authentication.RootCasException;

/**
 * Generic ticket exception. Top of the AbstractTicketException hierarchy.
 *
 * @author Scott Battaglia
 * @since 4.2.0
 */
public abstract class AbstractTicketException extends RootCasException {
    private static final long serialVersionUID = -5128676415951733624L;

    /**
     * Instantiates a new ticket exception.
     *
     * @param code the code
     * @param throwable the throwable
     */
    public AbstractTicketException(final String code, final Throwable throwable) {
      super(code, throwable);
    }

    /**
     * Instantiates a new ticket exception.
     *
     * @param code the code
     */
    public AbstractTicketException(final String code) {
      super(code);
    }

    /**
     * Instantiates a new ticket exception.
     *
     * @param code the code
     * @param msg the msg
     */
    public AbstractTicketException(final String code, final String msg) {
      super(code, msg);
    }
}
