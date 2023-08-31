package org.apereo.cas.support.saml.services;

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
import org.opensaml.saml.saml2.metadata.RequestedAttribute;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AuthnRequestRequestedAttributesAttributeReleasePolicy}.
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
public class AuthnRequestRequestedAttributesAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

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
        val releaseAttributes = new HashMap<String, List<Object>>();
        getSamlAuthnRequest(context).ifPresent(authnRequest -> {
            if (authnRequest.getExtensions() != null) {
                authnRequest.getExtensions().getUnknownXMLObjects()
                    .stream()
                    .filter(RequestedAttribute.class::isInstance)
                    .map(RequestedAttribute.class::cast)
                    .filter(attr -> {
                        val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                        LOGGER.debug("Checking for requested attribute [{}] in metadata for [{}]", name, context.getRegisteredService().getName());
                        return attributes.containsKey(name);
                    })
                    .forEach(attr -> {
                        val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                        LOGGER.debug("Found requested attribute [{}] in metadata for [{}]", name, context.getRegisteredService().getName());
                        releaseAttributes.put(name, attributes.get(name));
                    });
            }
        });
        return authorizeReleaseOfAllowedAttributes(context, releaseAttributes);
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        val definitions = new ArrayList<String>();
        getSamlAuthnRequest(context).ifPresent(authnRequest -> {
            if (authnRequest.getExtensions() != null) {
                authnRequest.getExtensions().getUnknownXMLObjects()
                    .stream()
                    .filter(RequestedAttribute.class::isInstance)
                    .map(RequestedAttribute.class::cast)
                    .forEach(attr -> {
                        val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                        LOGGER.debug("Found requested attribute [{}] in metadata for [{}]", name, context.getRegisteredService().getName());
                        definitions.add(name);
                    });
            }
        });
        return definitions;
    }
}
