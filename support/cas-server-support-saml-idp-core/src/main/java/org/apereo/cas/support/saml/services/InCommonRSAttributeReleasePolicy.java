package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * This is {@link InCommonRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSAttributeReleasePolicy extends MetadataEntityAttributesAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;
    private List<String> allowedAttributes = CollectionUtils.wrapList("eduPersonPrincipalName",
            "eduPersonTargetedID", "email", "displayName", "givenName", "surname", "eduPersonScopedAffiliation");

    public InCommonRSAttributeReleasePolicy() {
        setAllowedAttributes(allowedAttributes);
    }
    
    @JsonIgnore
    @Override
    public String getEntityAttribute() {
        return "http://macedir.org/entity-category";
    }

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("http://refeds.org/category/research-and-scholarship");
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
