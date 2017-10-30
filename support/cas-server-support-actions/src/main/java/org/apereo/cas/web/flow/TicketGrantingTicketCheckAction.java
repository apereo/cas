package org.apereo.cas.web.flow;

import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketCheckAction.class);

    private CentralAuthenticationService centralAuthenticationService;

    public TicketGrantingTicketCheckAction() {
    }

    /**
     * Creates a new instance with the given ticket registry.
     *
     * @param centralAuthenticationService the central authentication service
     */
    public TicketGrantingTicketCheckAction(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Determines whether the TGT in the flow request context is valid.
     *
     * @param requestContext Flow request context.
     *
     * @return {@link #NOT_EXISTS}, {@link #INVALID}, or {@link #VALID}.
     */
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (!StringUtils.hasText(tgtId)) {
            return new Event(this, NOT_EXISTS);
        }

        String eventId = INVALID;
        try {
            final Ticket ticket = this.centralAuthenticationService.getTicket(tgtId, Ticket.class);
            if (ticket != null && !ticket.isExpired()) {
                eventId = VALID;
            }
        } catch (final AbstractTicketException e) {
            LOGGER.trace("Could not retrieve ticket id [{}] from registry.", e.getMessage());
        }
        return new Event(this, eventId);
    }
}
