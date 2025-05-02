package org.apereo.cas.authentication.exceptions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes an error condition where a principal could not be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class UnresolvedPrincipalException extends PrincipalException {

    @Serial
    private static final long serialVersionUID = 380456166081802820L;

    /**
     * Error message when there was no error that prevent principal resolution.
     */
    private static final String UNRESOLVED_PRINCIPAL = "No resolver produced a principal.";

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     *
     * @param authentication Authentication event.
     */
    public UnresolvedPrincipalException(final Authentication authentication) {
        super(UNRESOLVED_PRINCIPAL, authentication.getFailures(), authentication.getSuccesses());
    }

    public UnresolvedPrincipalException() {
        super(UNRESOLVED_PRINCIPAL, new HashMap<>(), new HashMap<>());
    }

    public UnresolvedPrincipalException(final Exception e) {
        super(e.getMessage(), new HashMap<>(), new HashMap<>());
    }


    /**
     * Instantiates a new Unresolved principal exception.
     * Successes are tracked as an empty map.
     *
     * @param handlerErrors the handler errors
     */
    public UnresolvedPrincipalException(final Map<String, Throwable> handlerErrors) {
        super(UNRESOLVED_PRINCIPAL, handlerErrors, new HashMap<>());
    }

    /**
     * Creates a new instance from an authentication event that was successful prior to principal resolution.
     * This form should be used when a resolver exception prevented principal resolution.
     *
     * @param authentication Authentication event.
     * @param cause          Exception that prevented principal resolution.
     */
    public UnresolvedPrincipalException(final Authentication authentication, final Exception cause) {
        super(cause.getMessage(), authentication.getFailures(), authentication.getSuccesses());
    }
}
