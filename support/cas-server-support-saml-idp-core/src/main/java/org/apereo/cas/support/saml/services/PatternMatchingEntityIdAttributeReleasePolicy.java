package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.RegexUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link PatternMatchingEntityIdAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
public class PatternMatchingEntityIdAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 2633701342213724854L;

    private String entityIds = RegexUtils.MATCH_NOTHING_PATTERN.pattern();

    private boolean fullMatch = true;

    private boolean reverseMatch;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(final Map<String, List<Object>> attributes,
                                                                              final SamlRegisteredService registeredService,
                                                                              final ApplicationContext applicationContext,
                                                                              final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                              final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                              final EntityDescriptor entityDescriptor,
                                                                              final Principal principal,
                                                                              final Service selectedService) {
        val pattern = RegexUtils.createPattern(this.entityIds);
        val entityID = entityDescriptor.getEntityID();
        val matcher = pattern.matcher(entityID);
        var matched = fullMatch ? matcher.matches() : matcher.find();
        LOGGER.debug("Pattern [{}] matched against [{}]? [{}]", pattern.pattern(), entityID, BooleanUtils.toStringYesNo(matched));

        if (reverseMatch) {
            matched = !matched;
            LOGGER.debug("Reversed match to be [{}]", BooleanUtils.toStringYesNo(matched));
        }

        if (matched) {
            return authorizeReleaseOfAllowedAttributes(principal, attributes, registeredService, selectedService);
        }
        return new HashMap<>(0);
    }
}
