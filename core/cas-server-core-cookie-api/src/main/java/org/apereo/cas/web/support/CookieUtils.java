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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CookieUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@UtilityClass
@Slf4j
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
        val rememberMeMaxAge = getCookieMaxAge(cookie.getRememberMeMaxAge());
        val builder = buildCookieGenerationContextBuilder(cookie);
        return builder.rememberMeMaxAge(rememberMeMaxAge).build();
    }

    /**
     * Gets cookie max age.
     *
     * @param maxAge the max age
     * @return the cookie max age
     */
    public static int getCookieMaxAge(final String maxAge) {
        if (NumberUtils.isCreatable(maxAge)) {
            return Integer.parseInt(maxAge);
        }
        return (int) Beans.newDuration(maxAge).toSeconds();
    }

    private static CookieGenerationContext.CookieGenerationContextBuilder buildCookieGenerationContextBuilder(
        final CookieProperties cookie) {

        return CookieGenerationContext.builder()
            .name(cookie.getName())
            .path(cookie.getPath())
            .maxAge(getCookieMaxAge(cookie.getMaxAge()))
            .secure(cookie.isSecure())
            .domain(cookie.getDomain())
            .sameSitePolicy(cookie.getSameSitePolicy())
            .httpOnly(cookie.isHttpOnly());
    }

    /**
     * Configure cookie path.
     *
     * @param request       the request
     * @param cookieBuilder the cookie builder
     */
    public static void configureCookiePath(final HttpServletRequest request, final CasCookieBuilder cookieBuilder) {
        val contextPath = request.getContextPath();
        val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

        val path = cookieBuilder.getCookiePath();
        if (StringUtils.isBlank(path)) {
            LOGGER.debug("Setting cookie path for cookie [{}] to: [{}]", cookieBuilder.getCookieName(), cookiePath);
            cookieBuilder.setCookiePath(cookiePath);
        } else {
            LOGGER.trace("Cookie domain is [{}] with path [{}] for cookie [{}]",
                cookieBuilder.getCookieDomain(), path, cookieBuilder.getCookieName());
        }
    }

    /**
     * Configure cookie path.
     *
     * @param context       the context
     * @param cookieBuilder the cookie builder
     */
    public static void configureCookiePath(final RequestContext context, final CasCookieBuilder cookieBuilder) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        configureCookiePath(request, cookieBuilder);
    }

    /**
     * Create set cookie header string.
     *
     * @param cookieValue      the cookie value
     * @param cookieProperties the cookie properties
     * @return the cookie header string
     */
    public static String createSetCookieHeader(final String cookieValue,
                                               final CookieProperties cookieProperties) {
        return ResponseCookie.from(cookieProperties.getName(), StringUtils.trimToNull(cookieValue))
            .domain(StringUtils.trimToNull(cookieProperties.getDomain()))
            .httpOnly(cookieProperties.isHttpOnly())
            .maxAge(CookieUtils.getCookieMaxAge(cookieProperties.getMaxAge()))
            .path(cookieProperties.getPath())
            .secure(cookieProperties.isSecure())
            .sameSite(cookieProperties.getSameSitePolicy())
            .build()
            .toString();
    }

    /**
     * Create set cookie header string.
     *
     * @param cookieValue             the cookie value
     * @param cookieGenerationContext the cookie generation context
     * @return the string
     */
    public static String createSetCookieHeader(
        final String cookieValue,
        final CookieGenerationContext cookieGenerationContext) {
        return ResponseCookie.from(cookieGenerationContext.getName(), StringUtils.trimToNull(cookieValue))
            .domain(StringUtils.trimToNull(cookieGenerationContext.getDomain()))
            .httpOnly(cookieGenerationContext.isHttpOnly())
            .maxAge(cookieGenerationContext.getMaxAge())
            .path(cookieGenerationContext.getPath())
            .secure(cookieGenerationContext.isSecure())
            .sameSite(cookieGenerationContext.getSameSitePolicy())
            .build()
            .toString();
    }
}
