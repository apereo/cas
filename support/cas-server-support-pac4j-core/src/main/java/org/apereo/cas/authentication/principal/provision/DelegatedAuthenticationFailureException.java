package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.RootCasException;

import java.io.Serial;

/**
 * This is {@link DelegatedAuthenticationFailureException}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DelegatedAuthenticationFailureException extends RootCasException {
    @Serial
    private static final long serialVersionUID = -9100339143998828033L;

    /**
     * Code description.
     */
    private static final String CODE = "DELEGATED_AUTHN_FAILURE";

    public DelegatedAuthenticationFailureException() {
        super(CODE);
    }
}
