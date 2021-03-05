package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;
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
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    /**
     * Factory to create the principal type.
     **/
    protected final PrincipalFactory principalFactory;

    /**
     * The services manager instance, as the entry point to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * Indicates whether this handler is able to support the credentials passed to
     * operate on it and validate. Default is true.
     */
    protected Predicate<Credential> credentialSelectionPredicate = credential -> true;

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
     * Instantiates a new Abstract authentication handler.
     *
     * @param name             Handler name.
     * @param servicesManager  the services manager.
     * @param principalFactory the principal factory
     * @param order            the order
     */
    protected AbstractAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                         final PrincipalFactory principalFactory, final Integer order) {
        this.name = StringUtils.isNotBlank(name) ? name : getClass().getSimpleName();
        this.servicesManager = servicesManager;
        this.principalFactory = Objects.requireNonNullElseGet(principalFactory, DefaultPrincipalFactory::new);
        this.order = Objects.requireNonNullElseGet(order, () -> RandomUtils.nextInt(1, Integer.MAX_VALUE));
    }
}
