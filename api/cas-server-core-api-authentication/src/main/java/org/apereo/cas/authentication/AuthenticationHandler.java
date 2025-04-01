package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.util.NamedObject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * An authentication handler authenticates a single credential. In many cases credentials are authenticated by
 * comparison with data in a system of record such as LDAP directory or database.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@FunctionalInterface
public interface AuthenticationHandler extends Ordered, NamedObject {

    /**
     * Attribute name containing collection of handler names that successfully authenticated credential.
     */
    String SUCCESSFUL_AUTHENTICATION_HANDLERS = "successfulAuthenticationHandlers";

    /**
     * Disabled authentication handler.
     *
     * @return the authentication handler
     */
    static AuthenticationHandler disabled() {
        return new AuthenticationHandler() {
            @Override
            public AuthenticationHandlerExecutionResult authenticate(final Credential credential, final Service service) throws PreventedException {
                throw new PreventedException("Authentication handler is disabled");
            }

            @Override
            public boolean supports(final Credential credential) {
                return false;
            }

            @Override
            public boolean supports(final Class<? extends Credential> clazz) {
                return false;
            }
        };
    }

    /**
     * Authenticates the given credential. There are three possible outcomes of this process, and implementers
     * MUST adhere to the following contract:
     *
     * <ol>
     * <li>Success -- return {@link AuthenticationHandlerExecutionResult}</li>
     * <li>Failure -- throw {@link GeneralSecurityException}</li>
     * <li>Indeterminate -- throw {@link PreventedException}</li>
     * </ol>
     *
     * @param credential The credential to authenticate.
     * @param service    the requesting service, if any.
     * @return A result object containing metadata about a successful authentication event that includes at a
     * minimum the name of the handler that authenticated the credential and some credential metadata. The following data
     * is optional: <ul> <li>{@link Principal}</li> <li>Messages issued by the handler about the credential (e.g. impending password expiration warning)</li> </ul>
     * @throws Throwable the throwable
     */
    AuthenticationHandlerExecutionResult authenticate(Credential credential, Service service) throws Throwable;

    /**
     * Determines whether the handler has the capability to authenticate the given credential. In practical terms,
     * the {@link #authenticate(Credential, Service)} method MUST be capable of processing a given credential if
     * {@code supports} returns true on the same credential.
     *
     * @param credential The credential to check.
     * @return True if the handler supports the Credential, false otherwise.
     */
    default boolean supports(final Credential credential) {
        return false;
    }

    /**
     * Supports credential class.
     *
     * @param clazz the clazz
     * @return true/false
     */
    default boolean supports(final Class<? extends Credential> clazz) {
        return false;
    }

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Define the state of the authentication handler.
     *
     * @return the state
     */
    default AuthenticationHandlerStates getState() {
        return AuthenticationHandlerStates.ACTIVE;
    }

    /**
     * Gets properties.
     *
     * @return the properties
     */
    default Map<String, Serializable> getTags() {
        return Map.of();
    }

    /**
     * Is disposable handler?.
     *
     * @return true/false
     */
    default boolean isDisposable() {
        return this instanceof DisposableBean
            && BooleanUtils.isTrue((Boolean) getTags().getOrDefault(DisposableBean.class.getName(), Boolean.FALSE));
    }

    /**
     * Mark disposable authentication handler.
     *
     * @return the authentication handler
     */
    @CanIgnoreReturnValue
    default AuthenticationHandler markDisposable() {
        getTags().put(DisposableBean.class.getName(), Boolean.TRUE);
        return this;
    }
}
