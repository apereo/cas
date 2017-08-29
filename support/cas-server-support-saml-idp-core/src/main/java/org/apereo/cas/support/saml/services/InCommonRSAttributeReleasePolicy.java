package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            "eduPersonTargetedID", "email", "displayName", "givenName", "surname",
            "eduPersonScopedAffiliation");

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
