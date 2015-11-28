package org.jasig.cas.mfa.exceptions;

import org.jasig.cas.authentication.Authentication;

/**
 * An exception to indicate that a mismatch has been found between authenticated principals.
 * Credentials that are resolved into principals throughout the authentication flow are required
 * to be recognized by the same identifier {@link org.jasig.cas.authentication.principal.Principal#getId()}.
 * <p>For instance, if credentials are resolved into Principal 'A' as part of the first
 * leg of the multifactor authentication, and the second leg then resolves the credentials to into a Principal
 * that is identified by 'B', this exception will be thrown.
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class UnknownPrincipalMatchException extends RuntimeException {
    private static final long serialVersionUID = -6572930326804074536L;

    private final Authentication authentication;

    /**
     * Initialize the exception with the authentication given.
     * @param authentication the authentication context associated with this error.
     */
    public UnknownPrincipalMatchException(final Authentication authentication) {
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

}
