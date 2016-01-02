package org.jasig.cas.authentication;

/**
 * Multifactor authentication exception that is thrown
 * when the requested authentication method cannot be accepted
 * or isn't support by this CAS server.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class UnrecognizedAuthenticationMethodException extends RuntimeException {

    private static final long serialVersionUID = 7880539766094343828L;

    private final String authenticationMethod;
    private final String code = "unrecognized.authentication.method";

    /**
     * Initialize the exception object.
     * @param authnMethod the authentication method requested
     */
    public UnrecognizedAuthenticationMethodException(final String authnMethod) {
        this.authenticationMethod = authnMethod;
    }

    public final String getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    public final String getCode() {
        return this.code;
    }
}
