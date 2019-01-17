package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opensaml.saml.saml2.core.Attribute;

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

    private static final List<String> ALLOWED_ATTRIBUTES = CollectionUtils.wrapList("eduPersonPrincipalName",
        "eduPersonTargetedID", "email", "displayName", "givenName", "surname", "eduPersonScopedAffiliation");

    public InCommonRSAttributeReleasePolicy() {
        setAllowedAttributes(ALLOWED_ATTRIBUTES);
    }

    @JsonIgnore
    @Override
    public String getEntityAttribute() {
        return "http://macedir.org/entity-category";
    }

    @JsonIgnore
    @Override
    public Set<String> getEntityAttributeValues() {
        return CollectionUtils.wrapSet("http://id.incommon.org/category/research-and-scholarship");
    }

    @JsonIgnore
    @Override
    public String getEntityAttributeFormat() {
        return Attribute.URI_REFERENCE;
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
