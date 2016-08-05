package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * Webflow action that checks whether the TGT in the request context is valid. There are three possible outcomes:
 *
 * <ol>
 *     <li>{@link #NOT_EXISTS} - TGT not found in flow request context.</li>
 *     <li>{@link #INVALID} TGT has expired or is not found in ticket registry.</li>
 *     <li>{@link #VALID} - TGT found in ticket registry and has not expired.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("ticketGrantingTicketCheckAction")
public class TicketGrantingTicketCheckAction extends AbstractAction {

    /**
     * TGT does not exist event ID={@value}.
     **/
    public static final String NOT_EXISTS = "notExists";

    /**
     * TGT invalid event ID={@value}.
     **/
    public static final String INVALID = "invalid";

    /**
     * TGT valid event ID={@value}.
     **/
    public static final String VALID = "valid";

    /**
     * The Central authentication service.
     */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;


    /**
     * Creates a new instance with the given ticket registry.
     *
     * @param centralAuthenticationService the central authentication service
     */
    @Autowired
    public TicketGrantingTicketCheckAction(@Qualifier("centralAuthenticationService")
                                               final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     *
     * @throws Exception in case ticket cannot be retrieved from the service layer
     * @return {@link #NOT_EXISTS}, {@link #INVALID}, or {@link #VALID}.
     */
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (!StringUtils.hasText(tgtId)) {
            return new Event(this, NOT_EXISTS);
        }

        String eventId = INVALID;
        try {
            final Ticket ticket = this.centralAuthenticationService.getTicket(tgtId, Ticket.class);
            
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticket;
            final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
            
            final Map<String, Service> services = ticketGrantingTicket.getServices();
            final Entry<String, Service> service = services.entrySet().iterator().next();
            final String serviceId = service.getValue().getId();
            final String tenantNameOfService = AuthUtils.extractTenantID(serviceId);
            final String tenantNameOfRequest = AuthUtils.extractTenantID(request);
            
            if (ticket != null && !ticket.isExpired() && tenantNameOfService.equals(tenantNameOfRequest)) {
                eventId = VALID;
            }
        } catch (final AbstractTicketException e) {
            logger.trace("Could not retrieve ticket id {} from registry.", e);
        }
        return new Event(this,  eventId);
    }
}
