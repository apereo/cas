package org.apereo.cas.configuration.model.support.generic;

/**
 * This is {@link RejectAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class RejectAuthenticationProperties {
    
    private String users;

    public String getUsers() {
        return users;
    }

    public void setUsers(final String users) {
        this.users = users;
    }
}
