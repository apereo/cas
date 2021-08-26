package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SurrogateLdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-authentication-ldap")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateLdapAuthenticationProperties")
public class SurrogateLdapAuthenticationProperties extends AbstractLdapSearchProperties {

    private static final long serialVersionUID = -3848837302921751926L;

    /**
     * LDAP search filter used to locate the surrogate account.
     */
    @RequiredProperty
    private String surrogateSearchFilter;

    /**
     * Attribute that must be found on the LDAP entry linked to the admin user
     * that tags the account as authorized for impersonation.
     */
    @RequiredProperty
    private String memberAttributeName;

    /**
     * A pattern that is matched against the attribute value of the admin user,
     * that allows for further authorization of the admin user and accounts qualified for impersonation.
     * The regular expression pattern is expected to contain at least a single group whose value on a
     * successful match indicates the qualified impersonated user by admin.
     */
    private String memberAttributeValueRegex;
}
