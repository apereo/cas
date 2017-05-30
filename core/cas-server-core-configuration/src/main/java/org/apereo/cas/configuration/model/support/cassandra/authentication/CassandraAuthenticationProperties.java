package org.apereo.cas.configuration.model.support.cassandra.authentication;

/**
 * This is {@link CassandraAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CassandraAuthenticationProperties extends BaseCassandraProperties {
    private String name;
    private Integer order;
    private String usernameAttribute;
    private String passwordAttribute;
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

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
}
