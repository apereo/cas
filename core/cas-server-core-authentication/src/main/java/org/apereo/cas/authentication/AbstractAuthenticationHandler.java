package org.apereo.cas.authentication;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all authentication handlers that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    /**
     * Instance of logging for subclasses.
     */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /**
     * The services manager instance, as the entry point to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * Configurable handler name.
     */
    private final String name;

    private Integer order;

    /**
     * Instantiates a new Abstract authentication handler.
     *
     * @param name Sets the authentication handler name. Authentication handler names SHOULD be unique within an
     * {@link AuthenticationManager}, and particular implementations
     * may require uniqueness. Uniqueness is a best practice generally.
     */
    public AbstractAuthenticationHandler(final String name, final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
        if (StringUtils.isNotBlank(name)) {
            this.name = name;
        } else {
            this.name = getClass().getSimpleName();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Sets order. If order is undefined, generates a random order value.
     * Since handlers are generally sorted by this order, it's important that
     * order numbers be unique on a best-effort basis.
     * @param order the order
     */
    public void setOrder(final Integer order) {
        this.order = order;
        ensureOrderIsProvidedIfNecessary();
    }

    @Override
    public int getOrder() {
        ensureOrderIsProvidedIfNecessary();
        return this.order;
    }

    private void ensureOrderIsProvidedIfNecessary() {
        if (this.order == null) {
            this.order = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        }
    }
}
