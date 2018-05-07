package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.resolver.impl.mfa.PrincipalAttributeMultifactorAuthenticationPolicyEventResolver;
import org.springframework.web.util.CookieGenerator;

import java.util.Map;

/**
 * This is {@link SurrogateMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SurrogateMultifactorAuthenticationPolicyEventResolver extends PrincipalAttributeMultifactorAuthenticationPolicyEventResolver {
    public SurrogateMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                 final CentralAuthenticationService centralAuthenticationService,
                                                                 final ServicesManager servicesManager,
                                                                 final TicketRegistrySupport ticketRegistrySupport,
                                                                 final CookieGenerator warnCookieGenerator,
                                                                 final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                 final MultifactorAuthenticationProviderSelector selector,
                                                                 final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
            warnCookieGenerator, authSelectionStrategies, selector, casProperties);
    }

    @Override
    protected Map<String, Object> getPrincipalAttributesForMultifactorAuthentication(final Principal principal) {
        if (SurrogatePrincipal.class.isInstance(principal)) {
            final var c = SurrogatePrincipal.class.cast(principal);
            final var attributes = c.getPrimary().getAttributes();
            attributes.putAll(c.getSurrogate().getAttributes());
            return attributes;
        }
        return super.getPrincipalAttributesForMultifactorAuthentication(principal);
    }
}
