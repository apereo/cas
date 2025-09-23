package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.RegexUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 2633701342213724854L;

    private String entityIds = RegexUtils.MATCH_NOTHING_PATTERN.pattern();

    private boolean fullMatch = true;

    private boolean reverseMatch;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val pattern = RegexUtils.createPattern(this.entityIds);
        val entityID = entityDescriptor.getEntityID();
        val matcher = pattern.matcher(entityID);
        var matched = fullMatch ? matcher.matches() : matcher.find();
        LOGGER.debug("Pattern [{}] matched against [{}]? [{}]",
            pattern.pattern(), entityID, BooleanUtils.toStringYesNo(matched));
        if (reverseMatch) {
            matched = !matched;
            LOGGER.debug("Reversed match to be [{}]", BooleanUtils.toStringYesNo(matched));
        }
        return matched ? authorizeReleaseOfAllowedAttributes(context, attributes) : new HashMap<>();
    }
}
