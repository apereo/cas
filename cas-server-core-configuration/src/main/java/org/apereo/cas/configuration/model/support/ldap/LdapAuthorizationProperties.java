package org.apereo.cas.configuration.model.support.ldap;

/**
 * This is {@link LdapAuthorizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class LdapAuthorizationProperties extends AbstractLdapProperties {
    /** Default role prefix. */
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    
    private String roleAttribute = "uugid";
    private String rolePrefix = DEFAULT_ROLE_PREFIX;
    private boolean allowMultipleResults;

    private String baseDn;

    private String searchFilter;

    public String getRoleAttribute() {
        return roleAttribute;
    }

    public void setRoleAttribute(final String roleAttribute) {
        this.roleAttribute = roleAttribute;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(final String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public boolean isAllowMultipleResults() {
        return allowMultipleResults;
    }

    public void setAllowMultipleResults(final boolean allowMultipleResults) {
        this.allowMultipleResults = allowMultipleResults;
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

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }
}
