package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action that checks whether the TGT in the request context is valid.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TicketGrantingTicketCheckAction extends AbstractAction {

    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     * @return webflow transition to indicate TGT status.
     */
    @Override
    public Event doExecute(final RequestContext requestContext) {
        val tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgtId)) {
            return new Event(this, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS);
        }
        try {
            val ticket = this.centralAuthenticationService.getTicket(tgtId, Ticket.class);
            if (ticket != null && !ticket.isExpired()) {
                return new Event(this, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID);
            }
        } catch (final AbstractTicketException e) {
            LOGGER.trace("Could not retrieve ticket id [{}] from registry.", e.getMessage());
        }
        return new Event(this, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID);
    }
}
