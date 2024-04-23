package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
@JsonFilter("AbstractLdapAuthenticationProperties")
public abstract class AbstractLdapAuthenticationProperties extends AbstractLdapSearchProperties {

    @Serial
    private static final long serialVersionUID = 3849857270054289852L;

    /**
     * The authentication type.
     * <ul>
     * <li>{@code AD} - Users authenticate with {@code sAMAccountName}. </li>
     *
     * <li>{@code AUTHENTICATED} - Manager bind/search type of authentication.
     * If {@code} principalAttributePassword}
     * is empty then a user simple bind is done to validate credentials. Otherwise the given
     * attribute is compared with the given {@code principalAttributePassword} using
     * the {@code SHA} encrypted value of it.</li>
     *
     * <li>{@code ANONYMOUS}: Similar semantics as {@code AUTHENTICATED} except no {@code bindDn}
     * and {@code bindCredential} may be specified to initialize the connection.
     * If {@code principalAttributePassword} is empty then a user simple bind is done
     * to validate credentials. Otherwise the given attribute is compared with
     * the given {@code principalAttributePassword} using the {@code SHA} encrypted value of it.</li>
     *
     * <li>DIRECT: Direct Bind - Compute user DN from format string and perform simple bind.
     * This is relevant when no search is required to compute the DN needed for a bind operation.
     * Use cases for this type are:
     * 1) All users are under a single branch in the directory, {@code e.g. ou=Users,dc=example,dc=org.}
     * 2) The username provided on the CAS login form is part of the DN, e.g.
     * {@code uid=%s,ou=Users,dc=example,dc=org}.</li>
     *
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
     * If this attribute is set, the value found in the first attribute
     * value will be used in place of the DN.
     */
    private String resolveFromAttribute;

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
