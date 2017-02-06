package org.apereo.cas.configuration.model.support.ldap;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link LdapAuthorizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class LdapAuthorizationProperties {
    /**
     * Default role prefix.
     */
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    private String roleAttribute = "uugid";
    private String rolePrefix = DEFAULT_ROLE_PREFIX;
    private boolean allowMultipleResults;

    private String groupAttribute;
    private String groupPrefix = StringUtils.EMPTY;
    private String groupFilter;
    private String groupBaseDn;

    private String baseDn;
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
