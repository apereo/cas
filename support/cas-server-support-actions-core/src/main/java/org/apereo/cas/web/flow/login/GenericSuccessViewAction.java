package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Action that should execute prior to rendering the generic-success login view.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GenericSuccessViewAction extends AbstractAction {
    private final CentralAuthenticationService centralAuthenticationService;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final CasConfigurationProperties casProperties;

    /**
     * Gets authentication principal.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @return the authentication principal, or {@link NullPrincipal}
     * if none was available.
     */
    public Optional<Authentication> getAuthentication(final String ticketGrantingTicketId) {
        try {
            val ticketGrantingTicket = centralAuthenticationService.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            return Optional.of(ticketGrantingTicket.getAuthentication());
        } catch (final InvalidTicketException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        LOGGER.warn("In the absence of valid ticket-granting ticket, the authentication cannot be determined");
        return Optional.empty();
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val redirectUrl = casProperties.getView().getDefaultRedirectUrl();
        if (StringUtils.isNotBlank(redirectUrl)) {
            val service = this.serviceFactory.createService(redirectUrl);
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            requestContext.getExternalContext().requestExternalRedirect(service.getId());
        } else {
            val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
            getAuthentication(tgt).ifPresent(authn -> {
                WebUtils.putAuthentication(authn, requestContext);
                val service = WebUtils.getService(requestContext);
                if (casProperties.getView().isAuthorizedServicesOnSuccessfulLogin()) {
                    val authorizedServices = servicesManager.getAllServices()
                        .stream()
                        .filter(registeredService -> {
                            try {
                                return RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                                    registeredService, authn.getPrincipal().getId(),
                                    (Map) CollectionUtils.merge(authn.getAttributes(), authn.getPrincipal().getAttributes()));
                            } catch (final Exception e) {
                                LOGGER.error(e.getMessage(), e);
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                    WebUtils.putAuthorizedServices(requestContext, authorizedServices);
                }
            });
        }
        return success();
    }
}
