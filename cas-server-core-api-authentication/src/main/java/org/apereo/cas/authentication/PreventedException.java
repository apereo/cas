package org.apereo.cas.authentication;

/**
 * Describes an error condition where authentication was prevented for some reason, e.g. communication
 * error with back-end authentication store.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PreventedException extends Exception {

    private static final long serialVersionUID = 4702274165911620708L;

    /**
     * Creates a new instance with the exception that prevented authentication.
     *
     * @param cause Error that prevented authentication.
     */
    public PreventedException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance with an explanatory message and the exception that prevented authentication.
     *
     * @param message Descriptive error message.
     * @param cause Error that prevented authentication.
     */
    public PreventedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
