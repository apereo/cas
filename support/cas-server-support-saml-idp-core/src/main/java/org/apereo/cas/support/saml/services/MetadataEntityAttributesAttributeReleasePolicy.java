package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link MetadataEntityAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MetadataEntityAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -3483733307124962357L;

    private String entityAttribute;

    private String entityAttributeFormat;

    private Set<String> entityAttributeValues = new LinkedHashSet<>(0);

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(final Map<String, List<Object>> attributes,
                                                                              final SamlRegisteredService registeredService,
                                                                              final ApplicationContext applicationContext,
                                                                              final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                              final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                              final EntityDescriptor entityDescriptor,
                                                                              final Principal principal,
                                                                              final Service selectedService) {
        val attr = new EntityAttributesPredicate.Candidate(getEntityAttribute(), getEntityAttributeFormat());
        attr.setValues(getEntityAttributeValues());
        LOGGER.trace("Loading entity attribute predicate filter for candidate [{}] with values [{}]", attr.getName(), attr.getValues());
        val predicate = new EntityAttributesPredicate(CollectionUtils.wrap(attr), true);
        if (predicate.apply(entityDescriptor)) {
            LOGGER.debug("Authorizing release of allowed attributes [{}] for entity id [{}]",
                attributes, entityDescriptor.getEntityID());
            return authorizeReleaseOfAllowedAttributes(principal, attributes, registeredService, selectedService);
        }
        LOGGER.debug("Unable to authorize attribute release for entity attribute category [{}] and value(s) [{}] to entity id [{}]",
            getEntityAttribute(), getEntityAttributeValues(), entityDescriptor.getEntityID());
        return new HashMap<>(0);
    }
}
