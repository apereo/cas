package org.apereo.cas.authentication.exceptions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Principal;

/**
 * Describes an error condition where non-identical principals have been resolved while authenticating
 * multiple credentials.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class MixedPrincipalException extends PrincipalException {

    /** Serialization version marker. */
    private static final long serialVersionUID = -9040132618070273997L;

    /** First resolved principal. */
    private final Principal first;

    /** Second resolved principal. */
    private final Principal second;

    /**
     * Creates a new instance from what would otherwise have been a successful authentication event and the two
     * disparate principals resolved.
     *
     * @param authentication Authentication event.
     * @param a First resolved principal.
     * @param b Second resolved principal.
     */
    public MixedPrincipalException(final Authentication authentication, final Principal a, final Principal b) {
        super(a + " != " + b, authentication.getFailures(), authentication.getSuccesses());
        this.first = a;
        this.second = b;
    }

    /**
     * Gets the first resolved principal.
     *
     * @return First resolved principal.
     */
    public Principal getFirst() {
        return this.first;
    }

    /**
     * Gets the second resolved principal.
     *
     * @return Second resolved principal.
     */
    public Principal getSecond() {
        return this.second;
    }
}
