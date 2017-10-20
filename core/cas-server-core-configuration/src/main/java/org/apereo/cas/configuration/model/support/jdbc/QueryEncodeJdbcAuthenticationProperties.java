package org.apereo.cas.configuration.model.support.jdbc;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link QueryEncodeJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
public class QueryEncodeJdbcAuthenticationProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = -6647373426301411768L;
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
     * Algorithm used for hashing.
     */
    private String algorithmName;
    /**
     * SQL query to execute and look up accounts.
     * Example: {@code SELECT * FROM table WHERE username=?}.
     */
    @RequiredProperty
    private String sql;

    /**
     * Password column name.
     */
    private String passwordFieldName = "password";
    /**
     * Field/column name that indicates the salt used for password hashing.
     */
    @RequiredProperty
    private String saltFieldName = "salt";
    /**
     * Column name that indicates whether account is expired.
     */
    private String expiredFieldName;
    /**
     * Column name that indicates whether account is disabled.
     */
    private String disabledFieldName;

    /**
     * Field/column name that indicates the number of iterations used for password hashing.
     */
    private String numberOfIterationsFieldName = "numIterations";
    /**
     * Default number of iterations for hashing.
     */
    private long numberOfIterations;
    /**
     * Static salt to be used for hashing.
     */
    private String staticSalt;

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

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

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(final String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    public String getPasswordFieldName() {
        return passwordFieldName;
    }

    public void setPasswordFieldName(final String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    public String getSaltFieldName() {
        return saltFieldName;
    }

    public void setSaltFieldName(final String saltFieldName) {
        this.saltFieldName = saltFieldName;
    }

    public String getExpiredFieldName() {
        return expiredFieldName;
    }

    public void setExpiredFieldName(final String expiredFieldName) {
        this.expiredFieldName = expiredFieldName;
    }

    public String getDisabledFieldName() {
        return disabledFieldName;
    }

    public void setDisabledFieldName(final String disabledFieldName) {
        this.disabledFieldName = disabledFieldName;
    }

    public String getNumberOfIterationsFieldName() {
        return numberOfIterationsFieldName;
    }

    public void setNumberOfIterationsFieldName(final String numberOfIterationsFieldName) {
        this.numberOfIterationsFieldName = numberOfIterationsFieldName;
    }

    public long getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(final long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public String getStaticSalt() {
        return staticSalt;
    }

    public void setStaticSalt(final String staticSalt) {
        this.staticSalt = staticSalt;
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
}
