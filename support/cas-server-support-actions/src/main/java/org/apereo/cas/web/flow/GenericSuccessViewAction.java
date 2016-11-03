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

    /**
     * Log instance for logging events, info, warnings, errors, etc.
     */
    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private CentralAuthenticationService centralAuthenticationService;
    private ServicesManager servicesManager;
    private ServiceFactory serviceFactory;

    private String redirectUrl;

    /**
     * Instantiates a new Generic success view action.
     *
     * @param centralAuthenticationService the central authentication service
     * @param servicesManager              the services manager
     * @param serviceFactory               the service factory
     */
    public GenericSuccessViewAction(final CentralAuthenticationService centralAuthenticationService,
                                    final ServicesManager servicesManager,
                                    final ServiceFactory serviceFactory) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.servicesManager = servicesManager;
        this.serviceFactory = serviceFactory;
    }
    
    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
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
            final TicketGrantingTicket ticketGrantingTicket =
                    this.centralAuthenticationService.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            return ticketGrantingTicket.getAuthentication().getPrincipal();
        } catch (final InvalidTicketException e) {
            logger.warn("Ticket-granting ticket [{}] cannot be found in the ticket registry.", e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        logger.warn("In the absence of valid TGT, the authentication principal cannot be determined. Returning {}",
                NullPrincipal.class.getSimpleName());
        return NullPrincipal.getInstance();
    }
}
