package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link AuthenticationResultBuilder}. It attempts to collect authentication objects
 * and will put the computed finalized primary {@link Authentication} into {@link AuthenticationResult}.
 * <strong>Concurrency semantics: implementations MUST be thread-safe.</strong>
 * Instances of this class should never be declared as a field. Rather they should always be passed around to methods that need them.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationResultBuilder extends Serializable {

    /**
     * Gets the initial authentication.
     *
     * @return the initial authentication
     */
    Optional<Authentication> getInitialAuthentication();

    /**
     * Collect authentication objects from any number of processed authentication transactions.
     *
     * @param authentication the authentication
     * @return the authentication result builder
     */
    AuthenticationResultBuilder collect(Authentication authentication);

    /**
     * Provided credentials immediately by the user.
     *
     * @param credential the credential
     * @return the authentication context builder
     */
    AuthenticationResultBuilder collect(Credential credential);

    /**
     * Build authentication result.
     *
     * @return the authentication result
     */
    AuthenticationResult build();

    /**
     * Build authentication result.
     *
     * @param service the service
     * @return the authentication result
     */
    AuthenticationResult build(Service service);
}
