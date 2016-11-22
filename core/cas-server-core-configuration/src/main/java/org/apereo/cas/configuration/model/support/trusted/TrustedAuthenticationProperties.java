package org.apereo.cas.configuration.model.support.trusted;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;

/**
 * This is {@link TrustedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class TrustedAuthenticationProperties extends PersonDirPrincipalResolverProperties {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
