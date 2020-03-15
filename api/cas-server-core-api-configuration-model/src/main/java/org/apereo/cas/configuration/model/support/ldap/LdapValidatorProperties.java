package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link LdapValidatorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class LdapValidatorProperties implements Serializable {

    private static final long serialVersionUID = 1150417354213235193L;

    /**
     * The following LDAP validators can be used to test connection health status:
     * <ul>
     * <li>{@code search}: Validates a connection is healthy by performing a search operation.
     * Validation is considered successful if the search result size is greater than zero.</li>
     * <li>{@code none}: No validation takes place.</li>
     * <li>{@code compare}: Validates a connection is healthy by performing a compare operation.</li>
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
    private String attributeValue = "top";

    /**
     * DN to compare to use for the compare validator.
     */
    private String dn = StringUtils.EMPTY;
}
