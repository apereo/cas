package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -3483733307124962357L;

    private String entityAttribute;

    private String entityAttributeFormat;

    private Set<String> entityAttributeValues = new LinkedHashSet<>();

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        LOGGER.trace("Loading entity attribute predicate filter for candidate [{}] with values [{}]",
            getEntityAttribute(), getEntityAttributeValues());
        val match = SamlIdPUtils.doesEntityDescriptorMatchEntityAttribute(entityDescriptor,
            List.of(MetadataEntityAttributeQuery.of(getEntityAttribute(), getEntityAttributeFormat(), getEntityAttributeValues())));
        if (match) {
            LOGGER.debug("Authorizing release of allowed attributes [{}] for entity id [{}]",
                attributes, entityDescriptor.getEntityID());
            return authorizeReleaseOfAllowedAttributes(context, attributes);
        }
        LOGGER.debug("Unable to authorize attribute release for entity attribute category [{}] and value(s) [{}] to entity id [{}]",
            getEntityAttribute(), getEntityAttributeValues(), entityDescriptor.getEntityID());
        return new HashMap<>();
    }
}
