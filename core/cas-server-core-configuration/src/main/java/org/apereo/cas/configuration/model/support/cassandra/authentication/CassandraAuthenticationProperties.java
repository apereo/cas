package org.apereo.cas.configuration.model.support.cassandra.authentication;

/**
 * This is {@link CassandraAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CassandraAuthenticationProperties {
    private String name;
    private Integer order;
    private String username;
    private String password;
    private String keyspace;
    private String contactPoints;
    private String localDc;
    private String consistencyLevel;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
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

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(final String keyspace) {
        this.keyspace = keyspace;
    }

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(final String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getLocalDc() {
        return localDc;
    }

    public void setLocalDc(final String localDc) {
        this.localDc = localDc;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(final String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }
}
