package org.apereo.cas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link CassandraProperties}.
 *
 * @author David Rodriguez
 * @since 5.2.0
 */
@ConfigurationProperties("cassandra")
public class CassandraProperties {

    private String contactPoints;
    private String username;
    private String password;

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(final String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}



