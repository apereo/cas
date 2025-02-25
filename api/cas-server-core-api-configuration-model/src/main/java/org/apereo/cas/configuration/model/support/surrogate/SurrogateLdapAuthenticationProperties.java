package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
public class SurrogateLdapAuthenticationProperties extends AbstractLdapSearchProperties {

    @Serial
    private static final long serialVersionUID = -3848837302921751926L;

    /**
     * LDAP search filter used to locate the surrogate account.
     * The query is expected to determine whether the primary user is authorized
     * to impersonate the given account. These fields may be referred to in the LDAP search
     * query via {@code {user}} and {@code {surrogate}} placeholders.
     * If the query result yields a value that points to an LDAP entry, impersonation is authorized
     * for the given accounts.
     * <p>An example might be <pre>(&(uid={user})(xyzMemberOf=actAs:{surrogate}))</pre>
     */
    @RequiredProperty
    private String surrogateSearchFilter;

    /**
     * Attribute that must be found on the LDAP entry linked to the admin user
     * that tags the account as authorized for impersonation.
     * All attribute values are then compared against the pattern you specify in {@link #getMemberAttributeValueRegex()}.
     */
    @RequiredProperty
    private String memberAttributeName;

    /**
     * A pattern that is matched against the attribute value of the admin user,
     * that allows for further authorization of the admin user and accounts qualified for impersonation.
     * The regular expression pattern is expected to contain at least a single group whose value on a
     * successful match indicates the qualified impersonated user by admin.
     */
    @RegularExpressionCapable
    private String memberAttributeValueRegex;

    /**
     * An optional LDAP validation filter that attempts to look for surrogate/impersonatee
     * account in LDAP once authorization has been granted via {@link #getSurrogateSearchFilter()}.
     * You can use this validation filter to ensure the surrogate/impersonatee does exist in LDAP.
     * The LDAP filter may use {@code {surrogate}} as a placeholder in the filter to locate the surrogate account.
     * <p>An example might be: <pre>(&(uid={surrogate})(authorized=TRUE))}</pre>
     */
    private String surrogateValidationFilter;
}
