package org.apereo.cas.authentication.exceptions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Principal;

import lombok.Getter;

import java.io.Serial;

/**
 * Describes an error condition where non-identical principals have been resolved while authenticating
 * multiple credentials.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
public class MixedPrincipalException extends PrincipalException {

    @Serial
    private static final long serialVersionUID = -9040132618070273997L;

    /**
     * First resolved principal.
     */
    private final Principal first;

    /**
     * Second resolved principal.
     */
    private final Principal second;

    /**
     * Creates a new instance from what would otherwise have been a successful authentication event and the two
     * disparate principals resolved.
     *
     * @param authentication Authentication event.
     * @param a              First resolved principal.
     * @param b              Second resolved principal.
     */
    public MixedPrincipalException(final Authentication authentication, final Principal a, final Principal b) {
        super(a + " != " + b, authentication.getFailures(), authentication.getSuccesses());
        this.first = a;
        this.second = b;
    }

}
