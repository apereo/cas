package org.apereo.cas.authentication;

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

    /** Instance of logging for subclasses. */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Factory to create the principal type. **/
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /** The services manager instance, as the entry point to the registry. **/
    protected ServicesManager servicesManager;

    /** Configurable handler name. */
    private String name;

    /**
     * Instantiates a new Abstract authentication handler.
     */
    public AbstractAuthenticationHandler() {}

    @Override
    public String getName() {
        return this.name != null ? this.name : getClass().getSimpleName();
    }

    /**
     * Sets the authentication handler name. Authentication handler names SHOULD be unique within an
     * {@link AuthenticationManager}, and particular implementations
     * may require uniqueness. Uniqueness is a best
     * practice generally.
     *
     * @param name Handler name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
