package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.RootCasException;

/**
 * Generic ticket exception. Top of the AbstractTicketException hierarchy.
 *
 * @author Scott Battaglia
 * @since 4.2.0
 */
public abstract class AbstractTicketException extends RootCasException {
    @Serial
    private static final long serialVersionUID = -5128676415951733624L;

    protected AbstractTicketException(final String code, final Throwable throwable) {
        super(code, throwable);
    }

    protected AbstractTicketException(final String code) {
        super(code);
    }

    protected AbstractTicketException(final String code, final String msg, final List<Object> args) {
        super(code, msg, args);
    }

    protected AbstractTicketException(final String code, final Throwable throwable, final List<Object> args) {
        super(code, throwable, args);
    }
}
