package org.apereo.cas.consent;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * This is {@link DefaultConsentEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultConsentEngine implements ConsentEngine {
    private static final long serialVersionUID = -617809298856160625L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsentEngine.class);
    private final ConsentRepository consentRepository;
    private final ConsentDecisionBuilder consentDecisionBuilder;

    public DefaultConsentEngine(final ConsentRepository consentRepository,
                                final ConsentDecisionBuilder consentDecisionBuilder) {
        this.consentRepository = consentRepository;
        this.consentDecisionBuilder = consentDecisionBuilder;
    }


    @Override
    public Pair<Boolean, ConsentDecision> isConsentRequiredFor(final Service service,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication) {
        final Map<String, Object> attributes = resolveConsentableAttributesFrom(authentication, service, registeredService);

        if (attributes == null || attributes.isEmpty()) {
            LOGGER.debug("Consent is conditionally ignored for service [{}] given no consentable attributes are found", registeredService.getName());
            return Pair.of(false, null);
        }

        LOGGER.debug("Locating consent decision for service [{}]", service);
        final ConsentDecision decision = findConsentDecision(service, registeredService, authentication);
        if (decision == null) {
            LOGGER.debug("No consent decision found; thus attribute consent is required");
            return Pair.of(true, null);
        }

        LOGGER.debug("Located consentable attributes for release [{}]", attributes.keySet());
        if (consentDecisionBuilder.doesAttributeReleaseRequireConsent(decision, attributes)) {
            LOGGER.debug("Consent is required based on past decision [{}] and attribute release policy for [{}]",
                    decision, registeredService.getName());
            return Pair.of(true, decision);
        }

        LOGGER.debug("Consent is not required yet for [{}]; checking for reminder options", service);
        final ChronoUnit unit = decision.getReminderTimeUnit();
        final LocalDateTime dt = decision.getCreatedDate().plus(decision.getReminder(), unit);
        final LocalDateTime now = LocalDateTime.now();

        LOGGER.debug("Reminder threshold date/time is calculated as [{}]", dt);
        if (now.isAfter(dt)) {
            LOGGER.debug("Consent is required based on reminder options given now at [{}] is after [{}]", now, dt);
            return Pair.of(true, decision);
        }

        LOGGER.debug("Consent is not required for service [{}]", service);
        return Pair.of(false, null);
    }

    @Audit(action = "SAVE_CONSENT",
            actionResolverName = "SAVE_CONSENT_ACTION_RESOLVER",
            resourceResolverName = "SAVE_CONSENT_RESOURCE_RESOLVER")
    @Override
    public ConsentDecision storeConsentDecision(final Service service,
                                                final RegisteredService registeredService,
                                                final Authentication authentication,
                                                final long reminder,
                                                final ChronoUnit reminderTimeUnit,
                                                final ConsentOptions options) {
        final Map<String, Object> attributes = resolveConsentableAttributesFrom(authentication, service, registeredService);
        final String principalId = authentication.getPrincipal().getId();

        ConsentDecision decision = findConsentDecision(service, registeredService, authentication);
        if (decision == null) {
            decision = consentDecisionBuilder.build(service, registeredService, principalId, attributes);
        } else {
            decision = consentDecisionBuilder.update(decision, attributes);
        }
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
    public Map<String, Object> resolveConsentableAttributesFrom(final ConsentDecision decision) {
        LOGGER.debug("Retrieving consentable attributes from existing decision made by [{}] for [{}]",
                decision.getPrincipal(), decision.getService());
        return this.consentDecisionBuilder.getConsentableAttributesFrom(decision);
    }

    @Override
    public Map<String, Object> resolveConsentableAttributesFrom(final Authentication authentication,
                                                                final Service service,
                                                                final RegisteredService registeredService) {
        LOGGER.debug("Retrieving consentable attributes for [{}]", registeredService);
        return registeredService.getAttributeReleasePolicy().getConsentableAttributes(authentication.getPrincipal(), service, registeredService);
    }
}
