package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.experimental.UtilityClass;
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
@UtilityClass
public class CookieUtils {

    /**
     * Build cookie retrieving generator.
     *
     * @param cookie the cookie
     * @return the cookie retrieving cookie generator
     */
    public static CasCookieBuilder buildCookieRetrievingGenerator(final CookieProperties cookie) {
        val context = buildCookieGenerationContext(cookie);
        return new CookieRetrievingCookieGenerator(context);
    }

    /**
     * Gets ticket granting ticket from request.
     *
     * @param ticketGrantingTicketCookieGenerator the ticket granting ticket cookie generator
     * @param ticketRegistry                      the ticket registry
     * @param request                             the request
     * @return the ticket granting ticket from request
     */
    public static TicketGrantingTicket getTicketGrantingTicketFromRequest(final CasCookieBuilder ticketGrantingTicketCookieGenerator,
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

    /**
     * Build cookie generation context.
     *
     * @param cookie the cookie
     * @return the cookie generation context
     */
    public static CookieGenerationContext buildCookieGenerationContext(final CookieProperties cookie) {
        return buildCookieGenerationContextBuilder(cookie).build();
    }

    /**
     * Build cookie generation context cookie.
     *
     * @param cookie the cookie
     * @return the cookie generation context
     */
    public static CookieGenerationContext buildCookieGenerationContext(final TicketGrantingCookieProperties cookie) {
        val rememberMeMaxAge = (int) Beans.newDuration(cookie.getRememberMeMaxAge()).getSeconds();
        val builder = buildCookieGenerationContextBuilder(cookie);
        return builder.rememberMeMaxAge(rememberMeMaxAge).build();
    }

    private static CookieGenerationContext.CookieGenerationContextBuilder buildCookieGenerationContextBuilder(final CookieProperties cookie) {
        return CookieGenerationContext.builder()
            .name(cookie.getName())
            .path(StringUtils.defaultString(cookie.getPath(), "/"))
            .maxAge(cookie.getMaxAge())
            .secure(cookie.isSecure())
            .domain(cookie.getDomain())
            .comment(cookie.getComment())
            .sameSitePolicy(cookie.getSameSitePolicy())
            .httpOnly(cookie.isHttpOnly());
    }
}
