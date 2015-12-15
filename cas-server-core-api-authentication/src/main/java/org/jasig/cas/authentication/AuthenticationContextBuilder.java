package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Service;

import java.io.Serializable;

/**
 * This is {@link AuthenticationContextBuilder}. It attempts to collect authentication objects
 * and will put the result into an authentication context. Implementations MUST BE thread-safe.
 * Instances of this class should never be declared as a field. Rather they should always be passed
 * around to methods that need them.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationContextBuilder extends Serializable {

    /**
     * Collect authentication context builder.
     *
     * @param authentication the authentication
     * @return the authentication context builder
     */
    AuthenticationContextBuilder collect(Authentication authentication);

    /**
     * Build authentication context.
     *
     * @return the authentication context
     */
    AuthenticationContext build();

    /**
     * Build authentication context.
     *
     * @param service the service
     * @return the authentication context
     */
    AuthenticationContext build(Service service);
}
