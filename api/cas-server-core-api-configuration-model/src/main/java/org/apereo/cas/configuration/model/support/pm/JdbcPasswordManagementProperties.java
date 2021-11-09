package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link JdbcPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-jdbc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("JdbcPasswordManagementProperties")
public class JdbcPasswordManagementProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 4746591112640513465L;

    /**
     * Password encoder properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * SQL query to change the password and update.
     */
    @RequiredProperty
    private String sqlChangePassword;

    /**
     * SQL query to locate the user email address.
     */
    @RequiredProperty
    private String sqlFindEmail;

    /**
     * SQL query to locate the user phone number.
     */
    @RequiredProperty
    private String sqlFindPhone;

    /**
     * SQL query to locate the user via email.
     */
    @RequiredProperty
    private String sqlFindUser;

    /**
     * SQL query to locate security questions for the account, if any.
     */
    private String sqlGetSecurityQuestions;

    /**
     * SQL query to update security questions for the account, if any.
     */
    private String sqlUpdateSecurityQuestions;

    /**
     * SQL query to delete security questions for the account, if any.
     */
    private String sqlDeleteSecurityQuestions;
}

