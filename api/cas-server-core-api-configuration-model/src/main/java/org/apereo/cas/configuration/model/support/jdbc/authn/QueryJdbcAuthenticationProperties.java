package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link QueryJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class QueryJdbcAuthenticationProperties extends BaseJdbcAuthenticationProperties {

    private static final long serialVersionUID = 7806132208223986680L;

    /**
     * SQL query to execute. Example: {@code SELECT * FROM table WHERE name=?}.
     */
    @RequiredProperty
    private String sql;

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
    private List<String> principalAttributeList = new ArrayList<>(0);
}
