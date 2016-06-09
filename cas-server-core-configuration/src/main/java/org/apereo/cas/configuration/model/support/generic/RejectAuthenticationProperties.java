package org.apereo.cas.configuration.model.support.generic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link RejectAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "reject.authn", ignoreUnknownFields = false)
public class RejectAuthenticationProperties {
    
    private String users;

    public String getUsers() {
        return users;
    }

    public void setUsers(final String users) {
        this.users = users;
    }
}
