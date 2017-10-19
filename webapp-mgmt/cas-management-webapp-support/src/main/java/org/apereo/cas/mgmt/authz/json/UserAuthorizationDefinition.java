package org.apereo.cas.mgmt.authz.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link UserAuthorizationDefinition}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class UserAuthorizationDefinition implements Serializable {
    private static final long serialVersionUID = 5612860879960019695L;

    private Set<String> roles = new LinkedHashSet<>();
    private Set<String> permissions = new LinkedHashSet<>();
    
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(final Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(final Set<String> permissions) {
        this.permissions = permissions;
    }

}
