package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for cas.principal.resolver.persondir.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.principal.resolver.persondir", ignoreUnknownFields = false)
public class PersonDirPrincipalResolverProperties {

    private String principalAttribute;

    private boolean returnNull = false;

    public String getPrincipalAttribute() {
        return principalAttribute;
    }

    public void setPrincipalAttribute(final String principalAttribute) {
        this.principalAttribute = principalAttribute;
    }

    public boolean isReturnNull() {
        return returnNull;
    }

    public void setReturnNull(final boolean returnNull) {
        this.returnNull = returnNull;
    }
}
