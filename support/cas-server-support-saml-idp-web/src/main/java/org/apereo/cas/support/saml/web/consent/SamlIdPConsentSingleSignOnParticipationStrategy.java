package org.apereo.cas.support.saml.web.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.consent.ConsentActivationStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPSingleSignOnParticipationStrategy;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

import lombok.val;

/**
 * This is {@link SamlIdPConsentSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class SamlIdPConsentSingleSignOnParticipationStrategy extends SamlIdPSingleSignOnParticipationStrategy {
    private final ConsentActivationStrategy consentActivationStrategy;

    public SamlIdPConsentSingleSignOnParticipationStrategy(
        final ServicesManager servicesManager,
        final TicketRegistrySupport ticketRegistrySupport,
        final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
        final ConsentActivationStrategy consentActivationStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.consentActivationStrategy = consentActivationStrategy;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val service = ssoRequest.getAttributeValue(Service.class.getName(), Service.class);
        val registeredService = ssoRequest.getAttributeValue(RegisteredService.class.getName(), RegisteredService.class);
        val authentication = ssoRequest.getAttributeValue(Authentication.class.getName(), Authentication.class);
        val consentRequired = consentActivationStrategy.isConsentRequired(service, registeredService,
            authentication, ssoRequest.getHttpServletRequest().orElse(null));
        return !consentRequired && super.isParticipating(ssoRequest);
    }
}
