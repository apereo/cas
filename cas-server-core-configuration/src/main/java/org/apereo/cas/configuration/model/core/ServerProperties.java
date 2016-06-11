package org.apereo.cas.configuration.model.core;

/**
 * This is {@link ServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ServerProperties {
    
    private String name = "https://cas.example.org:8443";
    private String prefix = name.concat("/cas");

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
    
    public String getLoginUrl() {
        return getPrefix().concat("/login");
    }

    public String getLogoutUrl() {
        return getPrefix().concat("/logout");
    }
}
