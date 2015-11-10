package org.jasig.cas.authentication;

/**
 * Describes an error condition where a principal could not be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class UnresolvedPrincipalException extends PrincipalException {

    /** Serialization version marker. */
    private static final long serialVersionUID = 380456166081802820L;

    /** Error message when there was no error that prevent principal resolution. */
    private static final String UNRESOLVED_PRINCIPAL = "No resolver produced a principal.";

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     *
     * @param authentication Authentication event.
     */
    public UnresolvedPrincipalException(final Authentication authentication) {
        super(UNRESOLVED_PRINCIPAL, authentication.getFailures(), authentication.getSuccesses());
    }

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     * This form should be used when a resolver exception prevented principal resolution.
     *
     * @param authentication Authentication event.
     * @param cause Exception that prevented principal resolution.
     */
    public UnresolvedPrincipalException(final Authentication authentication, final Exception cause) {
        super(cause.getMessage(), authentication.getFailures(), authentication.getSuccesses());
    }
}
