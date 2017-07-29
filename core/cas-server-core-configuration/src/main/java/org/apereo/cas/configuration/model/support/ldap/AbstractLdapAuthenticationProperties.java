package org.apereo.cas.configuration.model.support.ldap;

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
     * <li>Active Directory - Users authenticate with sAMAccountName. </li>
     * <li>Authenticated Search - Manager bind/search</li>
     * <li>Anonymous Search</li>
     * <li>Direct Bind: Direct Bind - Compute user DN from format string and perform simple bind.
     * This is relevant when no search is required to compute the DN needed for a bind operation.
     * Use cases for this type are:
     * 1) All users are under a single branch in the directory, <code>e.g. ou=Users,dc=example,dc=org.</code>
     * 2) The username provided on the CAS login form is part of the DN, e.g.
     * <code>uid=%s,ou=Users,dc=example,dc=org</code>.</li>
     * </ul>
     */
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
     * Example format might be <code>uid=%s,ou=people,dc=example,dc=org</code>.
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
     * Syntax is <code>cn={user}</code> or <code>cn={0}</code>.
     */
    private String userFilter;

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

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(final String userFilter) {
        this.userFilter = userFilter;
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
    
}
