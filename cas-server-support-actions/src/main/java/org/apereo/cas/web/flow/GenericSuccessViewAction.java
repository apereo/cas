package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Resource;

/**
 * Action that should execute prior to rendering the generic-success login view.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GenericSuccessViewAction extends AbstractAction {
    /** Log instance for logging events, info, warnings, errors, etc. */
    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    public GenericSuccessViewAction() {
    }

    /**
     * Instantiates a new Generic success view action.
     *
     * @param centralAuthenticationService the central authentication service
     */
    public GenericSuccessViewAction(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        return success(getAuthenticationPrincipal(tgt));
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
        } catch (final InvalidTicketException e){
            logger.warn(e.getMessage());
        }
        logger.debug("In the absence of valid TGT, the authentication principal cannot be determined. Returning {}",
                NullPrincipal.class.getSimpleName());
        return NullPrincipal.getInstance();
    }
}
