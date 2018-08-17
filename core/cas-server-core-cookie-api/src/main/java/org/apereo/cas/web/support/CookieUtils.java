package org.apereo.cas.web.support;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link CookieUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
public class CookieUtils {

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
        val cookieValue = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            val tgt = ticketRegistry.getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null && !tgt.isExpired()) {
                return tgt;
            }
        }
        return null;
    }

    /**
     * Gets cookie from request.
     *
     * @param cookieName the cookie name
     * @param request    the request
     * @return the cookie from request
     */
    public static Optional<Cookie> getCookieFromRequest(final String cookieName, final HttpServletRequest request) {
        val cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies).filter(c -> c.getName().equalsIgnoreCase(cookieName)).findFirst();
    }
}
