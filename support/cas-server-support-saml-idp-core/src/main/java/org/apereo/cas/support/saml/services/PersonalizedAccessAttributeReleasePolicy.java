package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link PersonalizedAccessAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class PersonalizedAccessAttributeReleasePolicy extends BaseEntityCategoryAttributeReleasePolicy {
    /**
     * Map of allowed attributes by this policy in the form of attribute name linked
     * to its equivalent urn value.
     */
    public static final Map<String, String> ALLOWED_ATTRIBUTES = CollectionUtils.wrap(
        "sn", "urn:oid:2.5.4.4",
        "givenName", "urn:oid:2.5.4.42",
        "displayName", "urn:oid:2.16.840.1.113730.3.1.241",
        "mail", "urn:oid:0.9.2342.19200300.100.1.3",
        "eduPersonAssurance", "urn:oid:1.3.6.1.4.1.5923.1.1.1.11",
        "schacHomeOrganization", "urn:oid:1.3.6.1.4.1.25178.1.2.9",
        "eduPersonScopedAffiliation", "urn:oid:1.3.6.1.4.1.5923.1.1.1.9");
    
    @Serial
    private static final long serialVersionUID = 4885600380662937851L;

    @Override
    protected Map<String, String> getEntityCategoryAttributes() {
        return ALLOWED_ATTRIBUTES;
    }

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("https://refeds.org/category/personalized");
    }

}
