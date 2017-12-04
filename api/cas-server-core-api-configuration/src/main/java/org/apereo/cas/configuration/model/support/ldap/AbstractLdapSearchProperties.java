package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link AbstractLdapSearchProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class AbstractLdapSearchProperties extends AbstractLdapProperties {
    private static final long serialVersionUID = 3009946735155362639L;

    /**
     * Whether subtree searching is allowed.
     */
    private boolean subtreeSearch = true;

    /**
     * Base DN to use.
     */
    @RequiredProperty
    private String baseDn;

    /**
     * User filter to use for searching.
     * Syntax is {@code cn={user}} or {@code cn={0}}.
     */
    @RequiredProperty
    private String searchFilter;

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

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }
}
