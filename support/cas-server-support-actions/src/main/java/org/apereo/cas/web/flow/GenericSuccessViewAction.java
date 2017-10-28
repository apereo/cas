package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that should execute prior to rendering the generic-success login view.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GenericSuccessViewAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSuccessViewAction.class);
    private final CentralAuthenticationService centralAuthenticationService;
    private final ServicesManager servicesManager;
    private final ServiceFactory serviceFactory;

    private final String redirectUrl;

    /**
     * Instantiates a new Generic success view action.
     *
     * @param centralAuthenticationService the central authentication service
     * @param servicesManager              the services manager
     * @param serviceFactory               the service factory
     * @param redirectUrl                  the redirect url
     */
    public GenericSuccessViewAction(final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                    final ServiceFactory serviceFactory, final String redirectUrl) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.servicesManager = servicesManager;
        this.serviceFactory = serviceFactory;
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        if (StringUtils.isNotBlank(this.redirectUrl)) {
            final Service service = this.serviceFactory.createService(this.redirectUrl);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            requestContext.getExternalContext().requestExternalRedirect(service.getId());
        } else {
            final String tgt = WebUtils.getTicketGrantingTicketId(requestContext);
            WebUtils.putPrincipal(requestContext, getAuthenticationPrincipal(tgt));
        }
        return success();
    }

    /**
     * Gets authentication principal.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @return the authentication principal, or {@link NullPrincipal}
     * if none was available.
     */
    public Principal getAuthenticationPrincipal(final String ticketGrantingTicketId) {
        try {
            final TicketGrantingTicket ticketGrantingTicket = this.centralAuthenticationService.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            return ticketGrantingTicket.getAuthentication().getPrincipal();
        } catch (final InvalidTicketException e) {
            LOGGER.warn("Ticket-granting ticket [{}] cannot be found in the ticket registry.", e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
        LOGGER.warn("In the absence of valid TGT, the authentication principal cannot be determined. Returning [{}]", NullPrincipal.class.getSimpleName());
        return NullPrincipal.getInstance();
    }
}
