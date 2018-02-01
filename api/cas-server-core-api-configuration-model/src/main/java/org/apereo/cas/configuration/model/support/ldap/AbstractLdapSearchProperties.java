package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AbstractLdapSearchProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractLdapSearchProperties extends AbstractLdapProperties {
    private static final long serialVersionUID = 3009946735155362639L;

    /**
     * User filter to use for searching.
     * Syntax is {@code cn={user}} or {@code cn={0}}.
     */
    @RequiredProperty
    protected String searchFilter;

    /**
     * Whether subtree searching is allowed.
     */
    private boolean subtreeSearch = true;

    /**
     * Base DN to use.
     */
    @RequiredProperty
    private String baseDn;
}
