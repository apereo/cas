package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AbstractLdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ldap-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class AbstractLdapAuthenticationProperties extends AbstractLdapSearchProperties {

    private static final long serialVersionUID = 3849857270054289852L;
    /**
     * The authentication type.
     * <ul>
     * <li>AD - Users authenticate with sAMAccountName. </li>
     * <li>AUTHENTICATED - Manager bind/search</li>
     * <li>ANONYMOUS</li>
     * <li>DIRECT: Direct Bind - Compute user DN from format string and perform simple bind.
     * This is relevant when no search is required to compute the DN needed for a bind operation.
     * Use cases for this type are:
     * 1) All users are under a single branch in the directory, {@code e.g. ou=Users,dc=example,dc=org.}
     * 2) The username provided on the CAS login form is part of the DN, e.g.
     * {@code uid=%s,ou=Users,dc=example,dc=org}.</li>
     * </ul>
     */
    @RequiredProperty
    private AuthenticationTypes type = AuthenticationTypes.AUTHENTICATED;
    /**
     * If principalAttributePassword is empty then a user simple bind is done to validate credentials
     * otherwise the given attribute is compared with the given principalAttributePassword
     * using the SHA encrypted value of it.
     * <p>
     * For the anonymous authentication type,
     * if principalAttributePassword is empty then a user simple bind is done to validate credentials
     * otherwise the given attribute is compared with the given principalAttributePassword
     * using the SHA encrypted value of it.
     * </p>
     */
    private String principalAttributePassword;
    /**
     * Specify the dn format accepted by the AD authenticator, etc.
     * Example format might be {@code uid=%s,ou=people,dc=example,dc=org}.
     */
    private String dnFormat;
    /**
     * Whether specific search entry resolvers need to be set
     * on the authenticator, or the default should be used.
     */
    private boolean enhanceWithEntryResolver = true;
    /**
     * Define how aliases are de-referenced.
     * Accepted values are:
     * <ul>
     * <li>{@code NEVER}</li>
     * <li>{@code SEARCHING}: dereference when searching the entries beneath the starting point but not when searching for the starting entry.</li>
     * <li>{@code FINDING}: dereference when searching for the starting entry but not when searching the entries beneath the starting point.</li>
     * <li>{@code ALWAYS}: dereference when searching for the starting entry and when searching the entries beneath the starting point.</li>
     * </ul>
     */
    private String derefAliases;

    /**
     * The enum Authentication types.
     */
    public enum AuthenticationTypes {

        /**
         * Active Directory.
         */
        AD,
        /**
         * Authenticated Search.
         */
        AUTHENTICATED,
        /**
         * Direct Bind.
         */
        DIRECT,
        /**
         * Anonymous Search.
         */
        ANONYMOUS
    }
}
