package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("AbstractLdapSearchProperties")
public abstract class AbstractLdapSearchProperties extends AbstractLdapProperties {
    private static final long serialVersionUID = 3009946735155362639L;

    /**
     * User filter to use for searching.
     * Syntax is {@code cn={user}} or {@code cn={0}}.
     * 
     * You may also provide an external groovy script
     * in the syntax of {@code file:/path/to/GroovyScript.groovy}
     * to fully build the final filter template dynamically.
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
     * There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating
     * the LDAP configuration block for each individual base-dn, each entry can be specified
     * and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN
     * resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned.
     * Usual syntax is: {@code subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net}.
     */
    @RequiredProperty
    private String baseDn;

    /**
     * Search handlers.
     */
    private List<LdapSearchEntryHandlersProperties> searchEntryHandlers = new ArrayList<>(0);
}
