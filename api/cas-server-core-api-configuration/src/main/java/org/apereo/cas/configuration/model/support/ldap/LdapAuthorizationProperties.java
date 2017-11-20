package org.apereo.cas.configuration.model.support.ldap;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link LdapAuthorizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class LdapAuthorizationProperties implements Serializable {
    /**
     * Default role prefix.
     */
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    private static final long serialVersionUID = -2680169790567609780L;

    /**
     * Attribute expected to be found on the entry whose value is going to be used to
     * construct roles. The final value is always prefixed with {@link #rolePrefix}.
     * This is useful in scenarios where you wish to grant access to a resource to all
     * users who carry a special attribute.
     */
    private String roleAttribute = "uugid";

    /**
     * Prefix for the role.
     */
    private String rolePrefix = DEFAULT_ROLE_PREFIX;

    /**
     * Indicate whether the LDAP search query is allowed to return multiple entries.
     */
    private boolean allowMultipleResults;

    /**
     * Attribute expected to be found on the entry resulting from the group search whose value is going to be used to
     * construct roles. The final value is always prefixed with {@link #groupPrefix}.
     * This is useful in scenarios where you wish to grant access to a resource to all
     * users who a member of a given group.
     */
    private String groupAttribute;

    /**
     * A prefix that is prepended to the group attribute value to construct an authorized role.
     */
    private String groupPrefix = StringUtils.EMPTY;

    /**
     * Search filter to begin looking for groups.
     */
    private String groupFilter;

    /**
     * Base DN to start the search looking for groups.
     */
    private String groupBaseDn;

    /**
     * Base DN to start the search.
     */
    private String baseDn;
    /**
     * LDAP search filter to locate accounts.
     */
    private String searchFilter;

    public String getGroupBaseDn() {
        return StringUtils.defaultIfBlank(groupBaseDn, this.baseDn);
    }

    public void setGroupBaseDn(final String groupBaseDn) {
        this.groupBaseDn = groupBaseDn;
    }

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

    public String getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(final String groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(final String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    public String getGroupFilter() {
        return groupFilter;
    }

    public void setGroupFilter(final String groupFilter) {
        this.groupFilter = groupFilter;
    }
}
