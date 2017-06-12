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
    private String tgtTable;
    private String stTable;
    private String expiryTable;
    private String lastRunTable;

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

    public String getTgtTable() {
        return tgtTable;
    }

    public void setTgtTable(final String tgtTable) {
        this.tgtTable = tgtTable;
    }

    public String getStTable() {
        return stTable;
    }

    public void setStTable(final String stTable) {
        this.stTable = stTable;
    }

    public String getExpiryTable() {
        return expiryTable;
    }

    public void setExpiryTable(final String expiryTable) {
        this.expiryTable = expiryTable;
    }

    public String getLastRunTable() {
        return lastRunTable;
    }

    public void setLastRunTable(final String lastRunTable) {
        this.lastRunTable = lastRunTable;
    }
}



