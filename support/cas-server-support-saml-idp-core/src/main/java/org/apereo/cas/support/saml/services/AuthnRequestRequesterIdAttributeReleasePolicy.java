package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.RegexUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AuthnRequestRequesterIdAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class AuthnRequestRequesterIdAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -4273777707124962357L;

    @RegularExpressionCapable
    private String requesterIdPattern;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        val releaseAttributes = new HashMap<String, List<Object>>();
        getSamlAuthnRequest(context)
            .filter(authnRequest -> authnRequest.getScoping() != null)
            .filter(authnRequest -> !authnRequest.getScoping().getRequesterIDs().isEmpty())
            .ifPresent(authnRequest -> {
                val requesterIds = authnRequest.getScoping().getRequesterIDs();
                val matched = requesterIds
                    .stream()
                    .anyMatch(requesterId -> RegexUtils.find(this.requesterIdPattern, requesterId.getURI()));
                if (matched) {
                    releaseAttributes.putAll(attributes);
                }
            });
        return authorizeReleaseOfAllowedAttributes(context, releaseAttributes);

    }
}
