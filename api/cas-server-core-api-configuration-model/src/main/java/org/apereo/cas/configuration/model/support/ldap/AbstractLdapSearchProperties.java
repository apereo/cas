package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AbstractLdapSearchProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
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
     * Request that the server return results in batches of a
     * specific size. See <a href="http://www.ietf.org/rfc/rfc2696.txt">RFC 2696</a>. This control is often
     * used to work around server result size limits.
     * A negative/zero value disables paged requests.
     */
    private int pageSize;

    /**
     * Base DN to use.
     */
    @RequiredProperty
    private String baseDn;

    /**
     * Search handlers.
     */
    private List<LdapSearchEntryHandlersProperties> searchEntryHandlers = new ArrayList<>(0);
}
