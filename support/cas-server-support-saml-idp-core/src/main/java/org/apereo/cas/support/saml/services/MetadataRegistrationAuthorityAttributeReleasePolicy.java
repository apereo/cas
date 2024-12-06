package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
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
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -4273733307124962357L;

    private String registrationAuthority;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val extensions = Optional.ofNullable(facade.getExtensions())
            .map(ElementExtensibleXMLObject::getUnknownXMLObjects).orElseGet(List::of);

        val matched = extensions
            .stream()
            .filter(RegistrationInfo.class::isInstance)
            .map(RegistrationInfo.class::cast)
            .anyMatch(info -> RegexUtils.find(this.registrationAuthority, info.getRegistrationAuthority()));
        return matched ? authorizeReleaseOfAllowedAttributes(context, attributes) : new HashMap<>();
    }
}
