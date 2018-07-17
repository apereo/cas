package org.apereo.cas.support.saml.services;

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

import java.util.HashMap;
import java.util.Map;

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
    protected Map<String, Object> getAttributesForSamlRegisteredService(final Map<String, Object> attributes,
                                                                        final SamlRegisteredService service, final ApplicationContext applicationContext,
                                                                        final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                        final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                        final EntityDescriptor entityDescriptor) {
        val releaseAttributes = new HashMap<String, Object>();
        val sso = facade.getSsoDescriptor();
        if (sso != null) {
            sso.getAttributeConsumingServices().forEach(svc -> svc.getRequestAttributes().stream().filter(attr -> {
                val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                LOGGER.debug("Checking for requested attribute [{}] in metadata for [{}]", name, service.getName());
                return attributes.containsKey(name);
            }).forEach(attr -> {
                val name = this.useFriendlyName ? attr.getFriendlyName() : attr.getName();
                LOGGER.debug("Found requested attribute [{}] in metadata for [{}]", name, service.getName());
                releaseAttributes.put(name, attributes.get(name));
            }));
        }
        return releaseAttributes;
    }
}
