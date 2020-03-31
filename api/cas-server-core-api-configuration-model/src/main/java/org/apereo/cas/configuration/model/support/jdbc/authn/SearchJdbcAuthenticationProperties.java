package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SearchJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class SearchJdbcAuthenticationProperties extends BaseJdbcAuthenticationProperties {

    private static final long serialVersionUID = 6912107600297453730L;

    /**
     * Username column name.
     */
    @RequiredProperty
    private String fieldUser;

    /**
     * Password column name.
     */
    @RequiredProperty
    private String fieldPassword;

    /**
     * Table name where accounts are held.
     */
    @RequiredProperty
    private String tableUsers;
}
