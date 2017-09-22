package org.apereo.cas.configuration.model.support.openid;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class OpenIdProperties {
    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties principal = new PersonDirPrincipalResolverProperties();
    
    private boolean enforceRpId;

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

