package org.apereo.cas.consent;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.inspektr.audit.annotation.Audit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultConsentEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultConsentEngine implements ConsentEngine {
    private static final long serialVersionUID = -617809298856160625L;

    private final ConsentRepository consentRepository;

    public DefaultConsentEngine(final ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    @Override
    public Map<String, Object> getConsentableAttributes(final Authentication authentication,
                                                        final Service service,
                                                        final RegisteredService registeredService) {
        return registeredService.getAttributeReleasePolicy().getAttributes(authentication.getPrincipal(), service, registeredService);
    }

    @Override
    public Pair<Boolean, ConsentDecision> isConsentRequiredFor(final Service service,
                                                               final RegisteredService registeredService,
                                                               final Authentication authentication) {
        final ConsentDecision decision = findConsentDecision(service, registeredService, authentication);
        if (decision == null || decision.getOptions() == ConsentOptions.ALWAYS) {
            return Pair.of(true, decision);
        }

        return Pair.of(false, null);
    }

    @Audit(action = "SAVE_CONSENT",
            actionResolverName = "SAVE_CONSENT_ACTION_RESOLVER",
            resourceResolverName = "SAVE_CONSENT_RESOURCE_RESOLVER")
    @Override
    public ConsentDecision storeConsentDecision(final Service service, final RegisteredService registeredService,
                                                final Authentication authentication,
                                                final long reminder,
                                                final TimeUnit reminderTimeUnit,
                                                final ConsentOptions options) {
        final Map<String, Object> attributes = getConsentableAttributes(authentication, service, registeredService);
        final ConsentDecision decision = ConsentDecision.buildConsentDecision(service, registeredService,
                authentication.getPrincipal().getId(), attributes);
        decision.setOptions(options);
        decision.setReminder(reminder);
        decision.setReminderTimeUnit(reminderTimeUnit);
        if (consentRepository.storeConsentDecision(decision)) {
            return decision;
        }
        throw new IllegalArgumentException("Could not store consent decision");
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return consentRepository.findConsentDecision(service, registeredService, authentication);
    }
}
