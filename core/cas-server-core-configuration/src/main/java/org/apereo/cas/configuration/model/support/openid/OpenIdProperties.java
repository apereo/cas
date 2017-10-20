package org.apereo.cas.configuration.model.support.openid;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-openid")
public class OpenIdProperties implements Serializable {

    private static final long serialVersionUID = -2935759289483632610L;
    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Whether relying party identifies should be enforced.
     * This is used during the realm verification process.
     */
    private boolean enforceRpId;

    /**
     * Name of the underlying authentication handler.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isEnforceRpId() {
        return enforceRpId;
    }

    public void setEnforceRpId(final boolean enforceRpId) {
        this.enforceRpId = enforceRpId;
    }

    public PersonDirectoryPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirectoryPrincipalResolverProperties principal) {
        this.principal = principal;
    }
}

