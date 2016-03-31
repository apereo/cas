package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Action that should execute prior to rendering the generic-success login view.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Component("genericSuccessViewAction")
public final class GenericSuccessViewAction {
    /** Log instance for logging events, info, warnings, errors, etc. */
    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiates a new Generic success view action.
     *
     * @param centralAuthenticationService the central authentication service
     */
    @Autowired
    public GenericSuccessViewAction(@Qualifier("centralAuthenticationService")
                                        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Gets authentication principal.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @return the authentication principal, or {@link org.jasig.cas.authentication.principal.NullPrincipal}
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
