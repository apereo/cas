package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Base class for all authentication handlers that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"name", "state", "order"})
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    /**
     * Factory to create the principal type.
     **/
    protected final PrincipalFactory principalFactory;

    /**
     * The services manager instance, as the entry point to the registry.
     **/
    private final ServicesManager servicesManager;

    /**
     * Sets the authentication handler name. Authentication handler names SHOULD be unique within an
     * {@link AuthenticationManager}, and particular implementations
     * may require uniqueness. Uniqueness is a best
     * practice generally.
     */
    private final String name;

    /**
     * Sets order. If order is undefined, generates a random order value.
     * Since handlers are generally sorted by this order, it's important that
     * order numbers be unique on a best-effort basis.
     */
    private final int order;

    /**
     * Indicates whether this handler is able to support the credentials passed to
     * operate on it and validate. Default is true.
     */
    private Predicate<Credential> credentialSelectionPredicate = credential -> true;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;

    protected AbstractAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory, final Integer order) {
        this.name = StringUtils.isNotBlank(name) ? name : getClass().getSimpleName();
        this.servicesManager = servicesManager;
        this.principalFactory = Objects.requireNonNullElseGet(principalFactory, DefaultPrincipalFactory::new);
        this.order = Objects.requireNonNullElseGet(order, () -> RandomUtils.nextInt(1, Integer.MAX_VALUE));
    }
}
