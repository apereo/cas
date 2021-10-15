package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.RegexUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.opensaml.core.xml.ElementExtensibleXMLObject;
import org.opensaml.saml.ext.saml2mdrpi.RegistrationInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link MetadataRegistrationAuthorityAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MetadataRegistrationAuthorityAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -4273733307124962357L;

    private String registrationAuthority;

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
        val extensions = Optional.ofNullable(facade.getExtensions())
            .map(ElementExtensibleXMLObject::getUnknownXMLObjects).orElse(List.of());

        val matched = extensions.stream()
            .filter(object -> object instanceof RegistrationInfo)
            .map(info -> (RegistrationInfo) info)
            .anyMatch(info -> RegexUtils.find(this.registrationAuthority, info.getRegistrationAuthority()));

        if (matched) {
            return authorizeReleaseOfAllowedAttributes(principal, attributes, registeredService, selectedService);
        }
        return new HashMap<>(0);
    }
}
