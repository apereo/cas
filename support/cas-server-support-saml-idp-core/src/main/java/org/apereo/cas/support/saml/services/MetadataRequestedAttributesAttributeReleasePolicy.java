package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
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
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final long serialVersionUID = -3483733307124962357L;

    private boolean useFriendlyName;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredService registeredService,
        final ApplicationContext applicationContext,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
        final EntityDescriptor entityDescriptor,
        final Principal principal,
        final Service selectedService) {
        return fetchRequestedAttributes(attributes, registeredService, facade);
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final Principal principal,
                                                                  final RegisteredService registeredService,
                                                                  final Service selectedService) {
        val entityId = getEntityIdFromRequest(selectedService);
        val facade = determineServiceProviderMetadataFacade((SamlRegisteredService) registeredService, entityId);
        return facade
            .map(SamlRegisteredServiceServiceProviderMetadataFacade::getSsoDescriptor)
            .map(sso -> sso.getAttributeConsumingServices()
                .stream()
                .map(svc -> svc.getRequestedAttributes().stream()
                    .map(attr -> this.useFriendlyName ? attr.getFriendlyName() : attr.getName())
                    .collect(Collectors.toList()))
                .flatMap(List::stream)
                .sorted()
                .distinct()
                .collect(Collectors.toList()))
            .orElse(new ArrayList<>());
    }

    private Map<String, List<Object>> fetchRequestedAttributes(final Map<String, List<Object>> attributes,
                                                               final SamlRegisteredService registeredService,
                                                               final SamlRegisteredServiceServiceProviderMetadataFacade facade) {
        val releaseAttributes = new HashMap<String, List<Object>>();
        Optional.ofNullable(facade.getSsoDescriptor())
            .ifPresent(sso -> {
                sso.getAttributeConsumingServices().forEach(svc -> svc.getRequestedAttributes().stream().filter(attr -> {
                    val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                    LOGGER.debug("Checking for requested attribute [{}] in metadata for [{}]", name, registeredService.getName());
                    return attributes.containsKey(name);
                }).forEach(attr -> {
                    val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                    LOGGER.debug("Found requested attribute [{}] in metadata for [{}]", name, registeredService.getName());
                    releaseAttributes.put(name, attributes.get(name));
                }));
            });
        return releaseAttributes;
    }
}
