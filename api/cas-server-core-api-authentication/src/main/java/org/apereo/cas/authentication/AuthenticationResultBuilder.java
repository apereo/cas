package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
     * Gets authentications.
     *
     * @return the authentications
     */
    Set<Authentication> getAuthentications();

    /**
     * Gets the initial authentication.
     *
     * @return the initial authentication
     */
    Optional<Authentication> getInitialAuthentication();

    /**
     * Gets initial credential.
     *
     * @return the initial credential
     */
    Optional<Credential> getInitialCredential();

    /**
     * Collect authentication objects from any number of processed authentication transactions.
     *
     * @param authentication the authentication
     * @return the authentication result builder
     */
    AuthenticationResultBuilder collect(Authentication authentication);

    /**
     * Collect authentication result builder.
     *
     * @param authentications the authentication
     * @return the authentication result builder
     */
    AuthenticationResultBuilder collect(Collection<Authentication> authentications);

    /**
     * Provided credentials immediately by the user.
     *
     * @param credential the credential
     * @return the authentication context builder
     */
    AuthenticationResultBuilder collect(Credential... credential);

    /**
     * Build authentication result.
     *
     * @return the authentication result
     * @throws Throwable the throwable
     */
    default AuthenticationResult build() throws Throwable {
        return build(null);
    }

    /**
     * Build authentication result.
     *
     * @param service the service
     * @return the authentication result
     * @throws Throwable the throwable
     */
    AuthenticationResult build(Service service) throws Throwable;
}
