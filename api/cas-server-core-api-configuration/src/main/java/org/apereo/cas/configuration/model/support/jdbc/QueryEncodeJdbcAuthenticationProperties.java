package org.apereo.cas.configuration.model.support.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link QueryEncodeJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Slf4j
@Getter
@Setter
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
}
