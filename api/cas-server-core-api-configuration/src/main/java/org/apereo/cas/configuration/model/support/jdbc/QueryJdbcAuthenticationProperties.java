package org.apereo.cas.configuration.model.support.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link QueryJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
public class QueryJdbcAuthenticationProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = 7806132208223986680L;
    /**
     * SQL query to execute. Example: {@code SELECT * FROM table WHERE name=?}.
     */
    @RequiredProperty
    private String sql;
    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate<Credential>}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    private String credentialCriteria;
    /**
     * Password field/column name to retrieve.
     */
    @RequiredProperty
    private String fieldPassword;
    /**
     * Boolean field that should indicate whether the account is expired.
     */
    private String fieldExpired;
    /**
     * Boolean field that should indicate whether the account is disabled.
     */
    private String fieldDisabled;
    /**
     * List of column names to fetch as user attributes.
     */
    private List principalAttributeList = new ArrayList();

    /**
     * Principal transformation settings for this authentication.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password encoding strategies for this authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    public List getPrincipalAttributeList() {
        return principalAttributeList;
    }

    public void setPrincipalAttributeList(final List principalAttributeList) {
        this.principalAttributeList = principalAttributeList;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = StringUtils.replace(sql, "{user}", "?");
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public String getCredentialCriteria() {
        return credentialCriteria;
    }

    public void setCredentialCriteria(final String credentialCriteria) {
        this.credentialCriteria = credentialCriteria;
    }

    public String getFieldPassword() {
        return fieldPassword;
    }

    public void setFieldPassword(final String fieldPassword) {
        this.fieldPassword = fieldPassword;
    }

    public String getFieldExpired() {
        return fieldExpired;
    }

    public void setFieldExpired(final String fieldExpired) {
        this.fieldExpired = fieldExpired;
    }

    public String getFieldDisabled() {
        return fieldDisabled;
    }

    public void setFieldDisabled(final String fieldDisabled) {
        this.fieldDisabled = fieldDisabled;
    }
}
