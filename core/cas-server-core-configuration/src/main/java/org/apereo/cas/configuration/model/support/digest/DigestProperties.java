package org.apereo.cas.configuration.model.support.digest;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DigestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class DigestProperties {

    private String realm = "CAS";

    private String authenticationMethod = "auth";

    private Map<String, String> users = new HashMap<>();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(final String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(final Map<String, String> users) {
        this.users = users;
    }
}


