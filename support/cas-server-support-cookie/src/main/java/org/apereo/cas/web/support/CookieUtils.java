package org.apereo.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link CookieUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CookieUtils {
    protected CookieUtils() {
    }

    /**
     * Gets ticket granting ticket from request.
     *
     * @param ticketGrantingTicketCookieGenerator the ticket granting ticket cookie generator
     * @param ticketRegistry                      the ticket registry
     * @param request                             the request
     * @return the ticket granting ticket from request
     */
    public static TicketGrantingTicket getTicketGrantingTicketFromRequest(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                                          final TicketRegistry ticketRegistry, final HttpServletRequest request) {
        final String cookieValue = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            final TicketGrantingTicket tgt = ticketRegistry.getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null && !tgt.isExpired()) {
                return tgt;
            }
        }
        return null;
    }
}
