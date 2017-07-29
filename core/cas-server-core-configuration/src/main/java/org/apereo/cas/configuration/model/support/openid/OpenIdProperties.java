package org.apereo.cas.configuration.model.support.openid;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class OpenIdProperties implements Serializable {

    private static final long serialVersionUID = -2935759289483632610L;
    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties principal = new PersonDirPrincipalResolverProperties();

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

    public PersonDirPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirPrincipalResolverProperties principal) {
        this.principal = principal;
    }
}

