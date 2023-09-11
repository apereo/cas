package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link InCommonRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSAttributeReleasePolicy extends BaseEntityCategoryAttributeReleasePolicy {
    /**
     * Map of allowed attributes by this policy in the form of attribute name linked
     * to its equivalent urn value.
     */
    public static final Map<String, String> ALLOWED_ATTRIBUTES = CollectionUtils.wrap(
        "eduPersonPrincipalName", "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
        "eduPersonTargetedID", "urn:oid:1.3.6.1.4.1.5923.1.1.1.10",
        "email", "urn:oid:0.9.2342.19200300.100.1.3",
        "mail", "urn:oid:0.9.2342.19200300.100.1.3",
        "displayName", "urn:oid:2.16.840.1.113730.3.1.241",
        "givenName", "urn:oid:2.5.4.42",
        "surname", "urn:oid:2.5.4.4",
        "sn", "urn:oid:2.5.4.4",
        "eduPersonScopedAffiliation", "urn:oid:1.3.6.1.4.1.5923.1.1.1.9");
    @Serial
    private static final long serialVersionUID = 7679741348026967862L;

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("http://id.incommon.org/category/research-and-scholarship");
    }
    
    @Override
    @JsonIgnore
    protected Map<String, String> getEntityCategoryAttributes() {
        return ALLOWED_ATTRIBUTES;
    }
}
