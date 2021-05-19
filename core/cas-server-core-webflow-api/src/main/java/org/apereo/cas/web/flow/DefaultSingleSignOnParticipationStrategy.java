package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.model.TriStateBoolean;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class DefaultSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    private final SingleSignOnProperties properties;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public DefaultSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final SingleSignOnProperties properties,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.properties = properties;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        if (properties.isRenewAuthnEnabled() && ssoRequest.isRequestingRenewAuthentication()) {
            LOGGER.debug("The authentication session is considered renewed.");
            return false;
        }

        val registeredService = getRegisteredService(ssoRequest);
        if (registeredService == null) {
            return properties.isSsoEnabled();
        }

        val authentication = getAuthenticationFrom(ssoRequest);
        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            val isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso();
            LOGGER.trace("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                registeredService.getServiceId(), isAllowedForSso);

            if (!isAllowedForSso) {
                LOGGER.debug("Service [{}] is not authorized to participate in SSO", registeredService.getServiceId());
                return false;
            }

            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                val ticketState = getTicketState(ssoRequest);
                if (ticketState.isPresent()) {
                    return ssoPolicy.shouldParticipateInSso(registeredService, ticketState.get());
                }
            }

            val tgtPolicy = registeredService.getTicketGrantingTicketExpirationPolicy();
            if (tgtPolicy != null) {
                val ticketState = getTicketState(ssoRequest);
                return tgtPolicy.toExpirationPolicy()
                    .map(policy -> !policy.isExpired(ticketState.get())).orElse(Boolean.TRUE);
            }
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return true;
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        val registeredService = getRegisteredService(context);
        if (registeredService != null) {
            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                return ssoPolicy.getCreateCookieOnRenewedAuthentication();
            }
        }
        return TriStateBoolean.fromBoolean(properties.isCreateSsoCookieOnRenewAuthn());
    }
}
