package org.apereo.cas.configuration.model.support.scim;

/**
 * This is {@link ScimProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScimProperties {
    private long version = 2;
    private String target;
    private String oauthToken;
    private String username;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(final long version) {
        this.version = version;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(final String oauthToken) {
        this.oauthToken = oauthToken;
    }
}
