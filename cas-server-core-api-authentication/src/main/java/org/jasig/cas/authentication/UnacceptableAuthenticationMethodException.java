package org.jasig.cas.authentication;

/**
 * Multifactor authentication exception that is thrown
 * when the request authentication method cannot be accepted/provided
 * by this CAS server.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class UnacceptableAuthenticationMethodException extends RuntimeException {
    private static final long serialVersionUID = 7880539766094343828L;

    private final String authenticationMethod;
    private final String code = "unacceptable.authentication.method";

    /**
     * Initialize the exception object.
     * @param msg the error message describing this exception
     * @param authnMethod the authentication method requested
     */
    public UnacceptableAuthenticationMethodException(final String msg, final String authnMethod) {
        this.authenticationMethod = authnMethod;
    }

    public final String getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    public final String getCode() {
        return this.code;
    }
}
