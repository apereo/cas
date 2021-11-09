package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

import lombok.val;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;

/**
 * This is {@link SamlIdPSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class SamlIdPSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    public SamlIdPSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val authnRequest = ssoRequest.getAttributeValue(AuthnRequest.class.getName(), AuthnRequest.class);
        return supports(ssoRequest) && !authnRequest.isForceAuthn();
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.containsAttribute(AuthnRequest.class.getName())
            && ssoRequest.containsAttribute(Issuer.class.getName());
    }
}
