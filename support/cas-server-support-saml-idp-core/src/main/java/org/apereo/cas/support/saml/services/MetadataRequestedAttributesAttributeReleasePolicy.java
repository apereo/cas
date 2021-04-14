package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link MetadataRequestedAttributesAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString(callSuper = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetadataRequestedAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -3483733307124962357L;

    @Override
    protected List<String> getRequestedDefinitions(final RegisteredService registeredService) {
        val requestedDefinitions = new ArrayList<String>();
        if (registeredService instanceof SamlRegisteredService) {
            val samlRegisteredService = (SamlRegisteredService) registeredService;

            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val entityId = getEntityIdFromRequest(request);
            if (StringUtils.isBlank(entityId)) {
                LOGGER.warn("Could not locate the entity id for SAML attribute release policy processing");
                return new ArrayList<String>();
            }

            val applicationContext = ApplicationContextProvider.getApplicationContext();
            if (applicationContext == null) {
                LOGGER.warn("Could not locate the application context to process attributes");
                return new ArrayList<String>();
            }
            val resolver = applicationContext.getBean("defaultSamlRegisteredServiceCachingMetadataResolver",
                SamlRegisteredServiceCachingMetadataResolver.class);
            val facade = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlRegisteredService, entityId).get();

            val sso = facade.getSsoDescriptor();
            if (sso != null) {
                val urnsMap = getUrnsMap();

                sso.getAttributeConsumingServices().forEach(svc -> svc.getRequestedAttributes().stream().forEach(attr -> {
                    if (urnsMap.containsKey(attr.getName())) {
                        requestedDefinitions.add(urnsMap.get(attr.getName()));
                    }
                    requestedDefinitions.add(attr.getName());
                    requestedDefinitions.add(attr.getFriendlyName());
                }));
            }
        }
        return requestedDefinitions;
    }

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(final Map<String, List<Object>> attributes,
                                                                              final SamlRegisteredService registeredService,
                                                                              final ApplicationContext applicationContext,
                                                                              final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                              final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                              final EntityDescriptor entityDescriptor,
                                                                              final Principal principal,
                                                                              final Service selectedService) {
        val releaseAttributes = new HashMap<String, List<Object>>();
        val sso = facade.getSsoDescriptor();
        if (sso != null) {
            val urnsMap = getUrnsMap();
            sso.getAttributeConsumingServices().forEach(svc -> svc.getRequestedAttributes().stream().forEach(attr -> {
                val name = urnsMap.getOrDefault(attr.getName(), attr.getName());
                LOGGER.debug("Checking for requested attribute name [{}] or friendlyName [{}] in metadata for [{}]", attr.getName(), attr.getFriendlyName(), registeredService.getName());
                if (attributes.containsKey(name)) {
                    LOGGER.debug("Found requested attribute [{}] by name [{}] in metadata for [{}]", attr.getName(), name, registeredService.getName());
                    releaseAttributes.put(name, attributes.get(name));
                } else if (attributes.containsKey(attr.getFriendlyName())) {
                    LOGGER.debug("Found requested attribute friendly name [{}] in metadata for [{}]", attr.getFriendlyName(), registeredService.getName());
                    releaseAttributes.put(attr.getFriendlyName(), attributes.get(attr.getFriendlyName()));
                }
            }));
        }
        return releaseAttributes;
    }

    private Map<String, String> getUrnsMap() {
        LOGGER.trace("Retrieving attribute definition store and attribute definitions...");
        return ApplicationContextProvider.getAttributeDefinitionStore()
            .map(attributeDefinitionStore -> {
                val urnsMap = new HashMap<String, String>();

                attributeDefinitionStore.getAttributeDefinitions()
                    .stream()
                    .filter(defn -> defn instanceof SamlIdPAttributeDefinition)
                    .map(SamlIdPAttributeDefinition.class::cast)
                    .forEach(defn -> {
                        if (StringUtils.isNotBlank(defn.getUrn())) {
                            urnsMap.put(defn.getUrn(), defn.getKey());
                        }
                    });
                return urnsMap;
            })
            .orElseGet(() -> {
                LOGGER.trace("No attribute definition store is available in application context");
                return new HashMap<String, String>();
            });
    }
}

