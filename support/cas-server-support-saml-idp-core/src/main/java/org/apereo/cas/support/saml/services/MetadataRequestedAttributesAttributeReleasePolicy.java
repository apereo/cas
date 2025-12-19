package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * This is {@link MetadataRequestedAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetadataRequestedAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -3483733307124962357L;

    private boolean useFriendlyName;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        return fetchRequestedAttributes(attributes, context, facade);
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        val entityId = getEntityIdFromRequest(context);
        val facade = determineServiceProviderMetadataFacade(context, entityId);
        return facade
            .map(SamlRegisteredServiceMetadataAdaptor::getSsoDescriptor)
            .map(sso -> sso.getAttributeConsumingServices()
                .stream()
                .map(svc -> svc.getRequestedAttributes().stream()
                    .map(attr -> this.useFriendlyName ? attr.getFriendlyName() : attr.getName())
                    .collect(Collectors.toList()))
                .flatMap(List::stream)
                .sorted()
                .distinct()
                .collect(Collectors.toList()))
            .orElseGet(ArrayList::new);
    }

    private Map<String, List<Object>> fetchRequestedAttributes(final Map<String, List<Object>> attributes,
                                                               final RegisteredServiceAttributeReleasePolicyContext context,
                                                               final SamlRegisteredServiceMetadataAdaptor facade) {
        val releaseAttributes = new HashMap<String, List<Object>>();
        Optional.ofNullable(facade.getSsoDescriptor())
            .ifPresent(sso -> sso.getAttributeConsumingServices().forEach(svc -> svc.getRequestedAttributes().stream().filter(attr -> {
                val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                LOGGER.debug("Checking for requested attribute [{}] in metadata for [{}]",
                    name, context.getRegisteredService().getName());
                return attributes.containsKey(name);
            }).forEach(attr -> {
                val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                LOGGER.debug("Found requested attribute [{}] in metadata for [{}]",
                    name, context.getRegisteredService().getName());
                releaseAttributes.put(name, attributes.get(name));
            })));
        return releaseAttributes;
    }
}
