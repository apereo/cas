package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

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
     * @param cookie             the cookie
     * @param cookieValueManager the cookie value manager
     * @return the cookie retrieving cookie generator
     */
    public static CasCookieBuilder buildCookieRetrievingGenerator(final CookieProperties cookie,
                                                                  final CookieValueManager cookieValueManager) {
        val context = buildCookieGenerationContext(cookie);
        return buildCookieRetrievingGenerator(cookieValueManager, context);
    }

    /**
     * Build cookie retrieving generator cookie retrieving cookie generator.
     *
     * @param cookieValueManager the cookie value manager
     * @param context            the context
     * @return the cookie retrieving cookie generator
     */
    public static CookieRetrievingCookieGenerator buildCookieRetrievingGenerator(final CookieValueManager cookieValueManager,
                                                                                  final CookieGenerationContext context) {
        return new CookieRetrievingCookieGenerator(context, cookieValueManager);
    }

    /**
     * Build cookie retrieving generator cookie.
     *
     * @param context the context
     * @return the cookie retrieving cookie generator
     */
    public static CookieRetrievingCookieGenerator buildCookieRetrievingGenerator(final CookieGenerationContext context) {
        return buildCookieRetrievingGenerator(CookieValueManager.noOp(), context);
    }

    /**
     * Build cookie retrieving generator.
     *
     * @param cookie the cookie
     * @return the cookie retrieving cookie generator
     */
    public static CasCookieBuilder buildCookieRetrievingGenerator(final CookieProperties cookie) {
        return buildCookieRetrievingGenerator(cookie, CookieValueManager.noOp());
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
                                                                          final TicketRegistry ticketRegistry,
                                                                          final HttpServletRequest request) {
        val cookieValue = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            return FunctionUtils.doAndHandle(() -> {
                val state = ticketRegistry.getTicket(cookieValue, TicketGrantingTicket.class);
                return state == null || state.isExpired() ? null : state;
            });
        }
        return null;
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
     * Build cookie generation context.
     *
     * @param cookie the cookie
     * @return the cookie generation context
     */
    public static CookieGenerationContext buildCookieGenerationContext(final TicketGrantingCookieProperties cookie) {
        val rememberMeMaxAge = (int) Beans.newDuration(cookie.getRememberMeMaxAge()).getSeconds();
        val builder = buildCookieGenerationContextBuilder(cookie);
        return builder.rememberMeMaxAge(rememberMeMaxAge).build();
    }

    private static CookieGenerationContext.CookieGenerationContextBuilder buildCookieGenerationContextBuilder(
        final CookieProperties cookie) {
        
        return CookieGenerationContext.builder()
            .name(cookie.getName())
            .path(cookie.getPath())
            .maxAge(cookie.getMaxAge())
            .secure(cookie.isSecure())
            .domain(cookie.getDomain())
            .sameSitePolicy(cookie.getSameSitePolicy())
            .httpOnly(cookie.isHttpOnly());
    }
}
