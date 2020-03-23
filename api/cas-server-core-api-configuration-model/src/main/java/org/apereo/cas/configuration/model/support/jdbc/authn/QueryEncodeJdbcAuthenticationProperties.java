package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link QueryEncodeJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class QueryEncodeJdbcAuthenticationProperties extends BaseJdbcAuthenticationProperties {

    private static final long serialVersionUID = -6647373426301411768L;

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
    private int numberOfIterations;

    /**
     * Static salt to be used for hashing.
     */
    private String staticSalt;

}
