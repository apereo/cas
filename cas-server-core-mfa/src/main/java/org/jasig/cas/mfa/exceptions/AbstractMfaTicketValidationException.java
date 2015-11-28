package org.jasig.cas.mfa.exceptions;

/**
 * Base multifactor authentication exception class in the hierarchy.
 * Defines the authentication code, the error message and the requested
 * authentication method.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractMfaTicketValidationException extends RuntimeException {

    private static final long serialVersionUID = 7880539766094343828L;

    private final String authenticationMethod;
    private final String code;

    /**
     * Initialize the exception object.
     * @param c the error code
     * @param msg the error message describing this exception
     * @param authnMethod the authentication method requested
     */
    public AbstractMfaTicketValidationException(final String c, final String msg, final String authnMethod) {
        super(msg);
        this.code = c;
        this.authenticationMethod = authnMethod;
    }

    public final String getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    public final String getCode() {
        return this.code;
    }

}

