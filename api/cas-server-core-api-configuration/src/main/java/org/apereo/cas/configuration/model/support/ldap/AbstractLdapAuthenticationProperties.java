package org.apereo.cas.configuration.model.support.ldap;

import org.apache.commons.lang3.NotImplementedException;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AbstractLdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractLdapAuthenticationProperties extends AbstractLdapProperties {

    private static final long serialVersionUID = 3849857270054289852L;

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
    private AuthenticationTypes type;

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
     * Whether subtree searching is allowed.
     */
    private boolean subtreeSearch = true;
    
    /**
     * Base DN to use.
     */
    private String baseDn;

    /**
     * User filter to use for searching.
     * Syntax is {@code cn={user}} or {@code cn={0}}.
     */
    @RequiredProperty
    private String searchFilter;

    /**
     * Define how aliases are de-referenced.
     * Accepted values are:
     * <ul>
     *     <li>{@code NEVER}</li>
     *     <li>{@code SEARCHING}: dereference when searching the entries beneath the starting point but not when searching for the starting entry.</li>
     *     <li>{@code FINDING}: dereference when searching for the starting entry but not when searching the entries beneath the starting point.</li>
     *     <li>{@code ALWAYS}: dereference when searching for the starting entry and when searching the entries beneath the starting point.</li>
     * </ul>
     */
    private String derefAliases;
    
    /**
     * Search entry to define on the authenticator.
     */
    private List<LdapSearchEntryHandlersProperties> searchEntryHandlers = new ArrayList<>();

    public List<LdapSearchEntryHandlersProperties> getSearchEntryHandlers() {
        return searchEntryHandlers;
    }

    public void setSearchEntryHandlers(final List<LdapSearchEntryHandlersProperties> searchEntryHandlers) {
        this.searchEntryHandlers = searchEntryHandlers;
    }

    public AuthenticationTypes getType() {
        return type;
    }

    public void setType(final AuthenticationTypes type) {
        this.type = type;
    }

    public boolean isSubtreeSearch() {
        return subtreeSearch;
    }

    public void setSubtreeSearch(final boolean subtreeSearch) {
        this.subtreeSearch = subtreeSearch;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(final String userFilter) {
        this.searchFilter = userFilter;
    }

    public String getDnFormat() {
        return dnFormat;
    }

    public void setDnFormat(final String dnFormat) {
        this.dnFormat = dnFormat;
    }

    public boolean isEnhanceWithEntryResolver() {
        return enhanceWithEntryResolver;
    }

    public void setEnhanceWithEntryResolver(final boolean enhanceWithEntryResolver) {
        this.enhanceWithEntryResolver = enhanceWithEntryResolver;
    }

    public String getPrincipalAttributePassword() {
        return principalAttributePassword;
    }

    public void setPrincipalAttributePassword(final String principalAttributePassword) {
        this.principalAttributePassword = principalAttributePassword;
    }

    public String getDerefAliases() {
        return derefAliases;
    }

    public void setDerefAliases(final String derefAliases) {
        this.derefAliases = derefAliases;
    }

    /**
     * @deprecated Since 5.2. Use {{@link #setSearchFilter(String)} instead}.
     * Sets user filter.
     *
     * @param filter the filter
     */
    @Deprecated
    @DeprecatedConfigurationProperty(reason = "userFilter is replaced with searchFilter instead.", replacement = "searchFilter")
    public void setUserFilter(final String filter) {
        throw new NotImplementedException("userFilter is no longer supported. Use searchFilter instead");
    }

    /**
     * @deprecated Since 5.2. Use {{@link #getSearchFilter()} instead}.
     * Gets user filter.
     *
     * @return the user filter
     */
    @Deprecated
    @DeprecatedConfigurationProperty(reason = "userFilter is replaced with searchFilter instead.", replacement = "searchFilter")
    public String getUserFilter() {
        throw new NotImplementedException("userFilter is no longer supported. Use searchFilter instead");
    }
}
