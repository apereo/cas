package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * This is {@link MappedEntityAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MappedEntityAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -1253733307124962357L;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor adaptor,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        val query = List.of(MetadataEntityAttributeQuery.of(
            SamlIdPConstants.KnownEntityAttributes.SHIBBOLETH_SAML2_NAMING,
            Attribute.URI_REFERENCE));

        if (SamlIdPUtils.doesEntityDescriptorMatchEntityAttribute(adaptor.getEntityDescriptor(), query)) {
            val entityAttributes = SamlIdPUtils.collectEntityAttributes(adaptor.getEntityDescriptor(), query);
            entityAttributes
                .stream()
                .map(Attribute::getAttributeValues)
                .flatMap(Collection::stream)
                .filter(AttributeValue.class::isInstance)
                .map(AttributeValue.class::cast)
                .filter(attrValue -> StringUtils.isNotBlank(attrValue.getTextContent()))
                .forEach(attrValue -> {
                    val definedValue = Splitter.on(' ').splitToList(attrValue.getTextContent());
                    val originalName = definedValue.getFirst();
                    val renamedTo = definedValue.get(1);
                    if (attributes.containsKey(originalName)) {
                        attributes.put(renamedTo, attributes.remove(originalName));
                    }
                });
        }
        return attributes;
    }
}
