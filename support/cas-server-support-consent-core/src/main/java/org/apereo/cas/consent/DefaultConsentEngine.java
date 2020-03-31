package org.apereo.cas.consent;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultConsentEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultConsentEngine implements ConsentEngine {
    private static final long serialVersionUID = -617809298856160625L;

    private static final int MAP_SIZE = 8;

    private final ConsentRepository consentRepository;

    private final ConsentDecisionBuilder consentDecisionBuilder;

    private final SamlIdPProperties samlIdPProperties;

    private final AttributeDefinitionStore attributeDefinitionStore;

    @Audit(action = "SAVE_CONSENT",
        actionResolverName = "SAVE_CONSENT_ACTION_RESOLVER",
        resourceResolverName = "SAVE_CONSENT_RESOURCE_RESOLVER")
    @Override
    public ConsentDecision storeConsentDecision(final Service service,
                                                final RegisteredService registeredService,
                                                final Authentication authentication,
                                                final long reminder,
                                                final ChronoUnit reminderTimeUnit,
                                                final ConsentReminderOptions options) {
        val attributes = resolveConsentableAttributesFrom(authentication, service, registeredService);
        val principalId = authentication.getPrincipal().getId();

        val decisionFound = findConsentDecision(service, registeredService, authentication);

        val supplier = FunctionUtils.doIfNull(decisionFound,
            () -> consentDecisionBuilder.build(service, registeredService, principalId, attributes),
            () -> consentDecisionBuilder.update(decisionFound, attributes));

        val decision = supplier.get();
        decision.setOptions(options);
        decision.setReminder(reminder);
        decision.setReminderTimeUnit(reminderTimeUnit);

        if (consentRepository.storeConsentDecision(decision)) {
            return decision;
        }
        throw new IllegalArgumentException("Could not store consent decision");
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        return consentRepository.findConsentDecision(service, registeredService, authentication);
    }

    @Override
    public Map<String, List<Object>> resolveConsentableAttributesFrom(final Authentication authentication,
                                                                      final Service service,
                                                                      final RegisteredService registeredService) {
        LOGGER.debug("Retrieving consentable attributes for [{}]", registeredService);
        val policy = registeredService.getAttributeReleasePolicy();
        if (policy != null) {
            Map<String, List<Object>> attributes = policy.getConsentableAttributes(authentication.getPrincipal(), service, registeredService);

            // get friendly names if registeredService is instance of SamlRegisteredService
            if (registeredService instanceof SamlRegisteredService) {
                val friendlyAttributes = new HashMap<String, List<Object>>();
                val samlRegisteredService = (SamlRegisteredService) registeredService;
                val globalFriendlyNames = samlIdPProperties.getAttributeFriendlyNames();
                val friendlyNames = new HashMap<String, String>(CollectionUtils.convertDirectedListToMap(globalFriendlyNames));

                attributeDefinitionStore.getAttributeDefinitions()
                    .stream()
                    .filter(defn -> defn instanceof SamlIdPAttributeDefinition)
                    .map(SamlIdPAttributeDefinition.class::cast)
                    .filter(defn -> StringUtils.isNotBlank(defn.getFriendlyName()))
                    .forEach(defn -> friendlyNames.put(defn.getKey(), defn.getFriendlyName()));

                friendlyNames.putAll(samlRegisteredService.getAttributeFriendlyNames());

                for (val e : attributes.entrySet()) {
                    if (e.getValue() != null && ((Collection<?>) e.getValue()).isEmpty()) {
                        LOGGER.info("Skipping attribute [{}] because it does not have any values.", e.getKey());
                        continue;
                    }
                    val friendlyName = friendlyNames.getOrDefault(e.getKey(), e.getKey());

                    friendlyAttributes.put(friendlyName, e.getValue());
                }

                return friendlyAttributes;
            }

            return attributes;
        }
        return new LinkedHashMap<>(MAP_SIZE);
    }

    @Override
    public Map<String, List<Object>> resolveConsentableAttributesFrom(final ConsentDecision decision) {
        LOGGER.debug("Retrieving consentable attributes from existing decision made by [{}] for [{}]",
            decision.getPrincipal(), decision.getService());
        return this.consentDecisionBuilder.getConsentableAttributesFrom(decision);
    }

    @Override
    public ConsentQueryResult isConsentRequiredFor(final Service service,
                                                   final RegisteredService registeredService,
                                                   final Authentication authentication) {
        val attributes = resolveConsentableAttributesFrom(authentication, service, registeredService);

        if (attributes == null || attributes.isEmpty()) {
            LOGGER.debug("Consent is conditionally ignored for service [{}] given no consentable attributes are found", registeredService.getName());
            return ConsentQueryResult.ignored();
        }

        LOGGER.debug("Locating consent decision for service [{}]", service);
        val decision = findConsentDecision(service, registeredService, authentication);
        if (decision == null) {
            LOGGER.debug("No consent decision found; thus attribute consent is required");
            return ConsentQueryResult.required();
        }

        LOGGER.debug("Located consentable attributes for release [{}]", attributes.keySet());
        if (consentDecisionBuilder.doesAttributeReleaseRequireConsent(decision, attributes)) {
            LOGGER.debug("Consent is required based on past decision [{}] and attribute release policy for [{}]",
                decision, registeredService.getName());
            return ConsentQueryResult.required(decision);
        }

        LOGGER.debug("Consent is not required yet for [{}]; checking for reminder options", service);
        val unit = decision.getReminderTimeUnit();
        val dt = decision.getCreatedDate().plus(decision.getReminder(), unit);
        val now = LocalDateTime.now(ZoneId.systemDefault());

        LOGGER.debug("Reminder threshold date/time is calculated as [{}]", dt);
        if (now.isAfter(dt)) {
            LOGGER.debug("Consent is required based on reminder options given now at [{}] is after [{}]", now, dt);
            return ConsentQueryResult.required(decision);
        }

        LOGGER.debug("Consent is not required for service [{}]", service);
        return ConsentQueryResult.ignored();
    }
}
