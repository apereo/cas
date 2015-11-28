package org.jasig.cas.mfa.exceptions;

/**
 * Multifactor authentication exception that is thrown
 * when the requested authentication method cannot be accepted
 * or isn't support by this CAS server.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class UnrecognizedMfaMethodException extends RuntimeException {

    private static final long serialVersionUID = 7880539766094343828L;

    private final String authenticationMethod;
    private final String code = "unrecognized.authentication.method";

    /**
     * Initialize the exception object.
     * @param msg the error message describing this exception
     * @param authnMethod the authentication method requested
     */
    public UnrecognizedMfaMethodException(final String msg, final String authnMethod) {
        this.authenticationMethod = authnMethod;
    }

    public final String getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    public final String getCode() {
        return this.code;
    }
}
