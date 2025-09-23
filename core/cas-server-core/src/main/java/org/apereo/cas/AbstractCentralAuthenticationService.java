package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.LoggingUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.ObjectUtils;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * An abstract implementation of the {@link CentralAuthenticationService} that provides access to
 * the needed scaffolding and services that are necessary to CAS, such as ticket registry, service registry, etc.
 * The intention here is to allow extensions to easily benefit from these already-configured components
 * without having to duplicate them again.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService, Serializable {

    @Serial
    private static final long serialVersionUID = -7572316677901391166L;

    protected final CentralAuthenticationServiceContext configurationContext;

    @Override
    public TicketFactory getTicketFactory() {
        return this.configurationContext.getTicketFactory();
    }

    protected void doPublishEvent(final ApplicationEvent e) {
        if (configurationContext.getApplicationContext() != null) {
            LOGGER.trace("Publishing [{}]", e);
            configurationContext.getApplicationContext().publishEvent(e);
        }
    }

    protected Authentication getAuthenticationSatisfiedByPolicy(final Authentication authentication, final Service service,
                                                                final RegisteredService registeredService) throws AbstractTicketException {
        val policy = configurationContext.getAuthenticationPolicy();
        try {
            val policyContext = Map.of(RegisteredService.class.getName(), registeredService, Service.class.getName(), service);
            val executionResult = policy.isSatisfiedBy(authentication, configurationContext.getApplicationContext(), policyContext);
            if (executionResult.isSuccess()) {
                return authentication;
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new UnsatisfiedAuthenticationPolicyException(policy);
    }

    protected void evaluateProxiedServiceIfNeeded(final Service service, final TicketGrantingTicket ticketGrantingTicket, final RegisteredService registeredService) {
        val proxiedBy = ticketGrantingTicket.getProxiedBy();
        if (proxiedBy != null) {
            LOGGER.debug("Ticket-granting ticket is proxied by [{}]. Locating proxy service in registry...", proxiedBy.getId());
            val proxyingService = configurationContext.getServicesManager().findServiceBy(proxiedBy, CasModelRegisteredService.class);
            if (proxyingService != null) {
                LOGGER.debug("Located proxying service [{}] in the service registry", proxyingService);
                if (!proxyingService.getProxyPolicy().isAllowedToProxy()) {
                    LOGGER.warn("Proxying service [{}] is not authorized to fulfill the proxy attempt made by [{}]", proxyingService.getId(), service.getId());
                    throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE + registeredService.getId());
                }
            } else {
                LOGGER.warn("Proxy attempt by service [{}] (registered service [{}]) is not allowed.", service.getId(), registeredService.getId());
                throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE + registeredService.getId());
            }
        } else {
            LOGGER.trace("Ticket-granting ticket is not proxied by another service");
        }
    }

    protected Service resolveServiceFromAuthenticationRequest(final Service service) throws Throwable {
        return configurationContext.getAuthenticationServiceSelectionPlan().resolveService(service, Service.class);
    }

    protected boolean isTicketAuthenticityVerified(final String ticketId) {
        try {
            if (configurationContext.getCipherExecutor() != null) {
                LOGGER.trace("Attempting to decode service ticket [{}] to verify authenticity", ticketId);
                return !ObjectUtils.isEmpty(configurationContext.getCipherExecutor().decode(ticketId));
            }
            return !ObjectUtils.isEmpty(ticketId);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return false;
    }
}
