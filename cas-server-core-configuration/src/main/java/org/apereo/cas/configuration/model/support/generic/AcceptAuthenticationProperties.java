package org.apereo.cas.configuration.model.support.generic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link AcceptAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "accept.authn", ignoreUnknownFields = false)
public class AcceptAuthenticationProperties {
    
    private String users;

    public String getUsers() {
        return users;
    }

    public void setUsers(final String users) {
        this.users = users;
    }
}
