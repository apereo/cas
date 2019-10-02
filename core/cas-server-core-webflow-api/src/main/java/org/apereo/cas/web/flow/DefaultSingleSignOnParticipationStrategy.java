package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
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
@RequiredArgsConstructor
public class DefaultSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    @Getter
    private final ServicesManager servicesManager;

    private final boolean createCookieOnRenewedAuthentication;

    @Getter
    private final boolean renewEnabled;

    private final TicketRegistrySupport ticketRegistrySupport;

    @Setter
    @Getter
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        if (renewEnabled && requestContext.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.",
                CasProtocolConstants.PARAMETER_RENEW);
            return false;
        }

        // if service already resolved & stored in context, use that
        var registeredService = WebUtils.getRegisteredService(requestContext);

        if (registeredService == null) {
            val service = WebUtils.getService(requestContext);
            if (service != null) {
                registeredService = this.servicesManager.findServiceBy(service);
            }
        }
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
    public boolean isCreateCookieOnRenewedAuthentication(final RequestContext context) {
        return this.createCookieOnRenewedAuthentication;
    }
}
