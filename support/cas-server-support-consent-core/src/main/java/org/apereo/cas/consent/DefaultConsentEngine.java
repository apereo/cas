package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.util.Map;

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
    public boolean isConsentRequiredFor(final Service service,
                                        final RegisteredService registeredService,
                                        final Authentication authentication) {
        final ConsentDecision decision = consentRepository.findConsentDecision(service, registeredService, authentication);
        if (decision == null || decision.getOptions() == ConsentOptions.ALWAYS) {
            return true;
        }
        return false;
    }

    @Override
    public boolean storeConsentDecision(final Service service, final RegisteredService registeredService,
                                        final Authentication authentication) {
        final Map<String, Object> attributes = getConsentableAttributes(authentication, service, registeredService);
        final ConsentDecision decision = ConsentDecision.buildConsentDecision(service, registeredService,
                authentication.getPrincipal().getId(), attributes);
        return consentRepository.storeConsentDecision(decision);
    }
}
