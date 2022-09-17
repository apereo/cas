package org.apereo.cas.authentication;


import java.io.Serial;

/**
 * Describes an error condition where authentication was prevented for some reason, e.g. communication
 * error with back-end authentication store.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PreventedException extends RootCasException {

    /**
     * Code description.
     */
    public static final String CODE = "BLOCKED_AUTHN_REQUEST";

    @Serial
    private static final long serialVersionUID = 4702274165911620708L;

    public PreventedException(final String msg) {
        super(CODE, msg);
    }

    public PreventedException(final Throwable throwable) {
        super(CODE, throwable);
    }
}
