package org.apereo.cas.configuration.model.support.ldap;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link LdapValidatorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-ldap")
public class LdapValidatorProperties implements Serializable {
    private static final long serialVersionUID = 1150417354213235193L;
    /**
     * The following LDAP validators can be used to test connection health status:
     * <ul>
     *     <li>{@code search}: Validates a connection is healthy by performing a search operation.
     *     Validation is considered successful if the search result size is greater than zero.</li>
     *     <li>{@code none}: No validation takes place.</li>
     *     <li>{@code compare}: Validates a connection is healthy by performing a compare operation.</li>
     * </ul>
     */
    private String type = "search";
    /**
     * Base DN to use for the search request of the search validator.
     */
    private String baseDn = StringUtils.EMPTY;
    /**
     * Search filter to use for the search request of the search validator.
     */
    private String searchFilter = "(objectClass=*)";
    /**
     * Search scope to use for the search request of the search validator.
     */
    private String scope = "OBJECT";
    /**
     * Attribute name to use for the compare validator.
     */
    private String attributeName = "objectClass";
    /**
     * Attribute values to use for the compare validator.
     */
    private List<String> attributeValues = Stream.of("top").collect(Collectors.toList());
    /**
     * DN to compare to use for the compare validator.
     */
    private String dn = StringUtils.EMPTY;

    public String getDn() {
        return dn;
    }

    public void setDn(final String dn) {
        this.dn = dn;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public List<String> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(final List<String> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
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

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }
}
