package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InCommonRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InCommonRSAttributeReleasePolicy.class);

    private List<String> allowedAttributes = Arrays.asList("eduPersonPrincipalName",
            "eduPersonTargetedID", "email", "displayName", "givenName", "surname",
            "eduPersonScopedAffiliation");

    public InCommonRSAttributeReleasePolicy() {
        setAllowedAttributes(allowedAttributes);
    }
    
    @Override
    protected Map<String, Object> getAttributesForSamlRegisteredService(final Map<String, Object> attributes,
                                                                        final SamlRegisteredService service,
                                                                        final ApplicationContext applicationContext,
                                                                        final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                        final EntityDescriptor entityDescriptor) {
        final EntityAttributesPredicate.Candidate attr =
                new EntityAttributesPredicate.Candidate("http://macedir.org/entity-category");
        attr.setValues(Collections.singletonList("http://refeds.org/category/research-and-scholarship"));

        LOGGER.debug("Loading entity attribute predicate filter for candidate [{}] with values [{}]",
                attr.getName(), attr.getValues());

        final EntityAttributesPredicate predicate = new EntityAttributesPredicate(
                Collections.singletonList(attr), true);

        if (predicate.apply(entityDescriptor)) {
            return authorizeReleaseOfAllowedAttributes(attributes);
        }
        return new HashMap<>();
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
