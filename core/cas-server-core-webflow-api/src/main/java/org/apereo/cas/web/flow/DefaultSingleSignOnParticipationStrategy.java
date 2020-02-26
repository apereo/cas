package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@Setter
public class DefaultSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final ServicesManager servicesManager;

    private final boolean createCookieOnRenewedAuthentication;

    private final boolean renewEnabled;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        if (renewEnabled && requestContext.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.",
                CasProtocolConstants.PARAMETER_RENEW);
            return false;
        }

        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return true;
        }

        val authentication = WebUtils.getAuthentication(requestContext);
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
                val tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
                val ticketState = ticketRegistrySupport.getTicketState(tgtId);
                if (ticketState != null) {
                    return ssoPolicy.shouldParticipateInSso(ticketState);
                }
            }
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return true;
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final RequestContext context) {
        val registeredService = determineRegisteredService(context);
        if (registeredService != null) {
            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                return ssoPolicy.isCreateCookieOnRenewedAuthentication();
            }
        }
        return TriStateBoolean.fromBoolean(this.createCookieOnRenewedAuthentication);
    }

    private RegisteredService determineRegisteredService(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null) {
            return registeredService;
        }
        val service = WebUtils.getService(requestContext);
        val serviceToUse = serviceSelectionStrategy.resolveService(service);
        if (serviceToUse != null) {
            return this.servicesManager.findServiceBy(serviceToUse);
        }
        return null;
    }
}
