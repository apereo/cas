package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SurrogateJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-authentication-jdbc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateJdbcAuthenticationProperties")
public class SurrogateJdbcAuthenticationProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 8970195444880123796L;

    /**
     * Surrogate query to use to determine whether an admin user can impersonate another user.
     * The query must return an integer count of greater than zero.
     */
    @RequiredProperty
    private String surrogateSearchQuery = "SELECT COUNT(*) FROM surrogate WHERE username=?";

    /**
     * SQL query to use in order to retrieve the list of qualified accounts for impersonation for a given admin user.
     */
    @RequiredProperty
    private String surrogateAccountQuery = "SELECT surrogate_user AS surrogateAccount FROM surrogate WHERE username=?";
}
