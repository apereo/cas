package org.apereo.cas.configuration.model.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link ServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = false)
public class ServerProperties {
    
    private String name;
    private String prefix;

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
}
