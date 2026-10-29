package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Requires a user signed in to CAS for authorization and device approval requests. The profile that the login handshake
 * keeps in the session is used when it was established for the client of the request and the single sign-on session
 * behind it is still alive (or the authentication was stateless). Otherwise the request is saved in the session and the
 * browser is sent to the CAS login, with the callback endpoint as the service; a request made with
 * {@code XMLHttpRequest} is refused with {@code 401} instead. The callback validates the ticket, keeps the profile and
 * resumes the saved request. A {@code POST} request is saved as the {@code GET} request with the same parameters, which
 * the authorization endpoint accepts as well (OpenID Connect Core, section 3.1.2.1).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20CasAuthenticationInterceptor implements HandlerInterceptor {
    private static final String AJAX_HEADER_VALUE = "XMLHttpRequest";

    private static final String AJAX_HEADER_NAME = "X-Requested-With";

    private static final String AJAX_REQUEST_INDICATOR = "is_ajax_request";

    private static final String AJAX_AUTHENTICATION_CHALLENGE = "Bearer realm=\"CAS\"";

    protected final CasSessionStore sessionStore;

    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    protected final TicketRegistry ticketRegistry;

    protected final ServicesManager servicesManager;

    protected final OAuth20RequestParameterResolver requestParameterResolver;

    protected final OAuth20CasCallbackUrlResolver callbackUrlResolver;

    protected final OAuth20CasClientRedirectActionBuilder casClientRedirectActionBuilder;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        OAuth20Utils.addSecurityHeaders(request, response);
        val profile = findAuthenticatedProfile(request);
        if (profile.isPresent()) {
            LOGGER.trace("Request to [{}] is authenticated as [{}]", request.getRequestURI(), profile.get().getId());
            request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile.get());
            return true;
        }
        if (isAjaxRequest(request)) {
            LOGGER.debug("Request to [{}] is not authenticated and cannot be redirected to log in", request.getRequestURI());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, AJAX_AUTHENTICATION_CHALLENGE);
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        val requestedUrl = buildRequestedUrl(request);
        sessionStore.set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL, requestedUrl);
        val serviceUrl = callbackUrlResolver.compute(request);
        val loginUrl = casClientRedirectActionBuilder.build(request, serviceUrl);
        LOGGER.debug("Request to [{}] is not authenticated; redirecting to [{}]", requestedUrl, loginUrl);
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader(HttpHeaders.LOCATION, loginUrl);
        return false;
    }

    /**
     * Whether the request is made by a script that cannot follow a redirect to the login page: it carries the
     * {@code X-Requested-With: XMLHttpRequest} header, or sets {@code is_ajax_request} to {@code true} as a header or a parameter.
     *
     * @param request the request
     * @return true or false
     */
    protected boolean isAjaxRequest(final HttpServletRequest request) {
        return AJAX_HEADER_VALUE.equalsIgnoreCase(request.getHeader(AJAX_HEADER_NAME))
            || Boolean.TRUE.toString().equalsIgnoreCase(request.getHeader(AJAX_REQUEST_INDICATOR))
            || Boolean.TRUE.toString().equalsIgnoreCase(request.getParameter(AJAX_REQUEST_INDICATOR));
    }

    /**
     * The profile that the login handshake keeps in the session, when it may be used for this request.
     *
     * @param request the request
     * @return the profile
     */
    protected Optional<OAuth20UserProfile> findAuthenticatedProfile(final HttpServletRequest request) {
        return sessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE)
            .filter(OAuth20UserProfile.class::isInstance)
            .map(OAuth20UserProfile.class::cast)
            .filter(profile -> isAuthenticatedForClient(request, profile))
            .filter(profile -> isAuthenticationUsable(request, profile));
    }

    /**
     * Whether the profile was established for the client of the request, compared as client identifiers are looked up,
     * with case; a request that names no client accepts any.
     *
     * @param request the request
     * @param profile the profile
     * @return true or false
     */
    protected boolean isAuthenticatedForClient(final HttpServletRequest request, final OAuth20UserProfile profile) {
        val clientId = OAuth20Utils.resolveClientIdFromRequest(request, servicesManager, requestParameterResolver);
        return StringUtils.isBlank(clientId)
            || Strings.CS.equals(String.valueOf(profile.getAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID)), clientId);
    }

    /**
     * Whether the authentication behind the profile may still be used: the request carries the cookie of a single sign-on
     * session that is still alive, or the authentication was stateless.
     *
     * @param request the request
     * @param profile the profile
     * @return true or false
     */
    protected boolean isAuthenticationUsable(final HttpServletRequest request, final OAuth20UserProfile profile) {
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(ticketGrantingTicketCookieGenerator, ticketRegistry, request);
        if (ticketGrantingTicket == null && !OAuth20Utils.isStatelessAuthentication(profile)) {
            LOGGER.debug("No ticket-granting ticket is found for the request; the profile in the session is not used");
            return false;
        }
        return true;
    }

    /**
     * The URL of the request, to resume after login: the full URL of a {@code GET} request, or the URL with the parameters
     * of any other request.
     *
     * @param request the request
     * @return the requested url
     */
    protected String buildRequestedUrl(final HttpServletRequest request) {
        if (HttpMethod.GET.matches(request.getMethod())) {
            return HttpRequestUtils.getFullRequestUrl(request);
        }
        return FunctionUtils.doUnchecked(() -> {
            val builder = new URIBuilder(request.getRequestURL().toString());
            request.getParameterMap().forEach((name, values) -> Arrays.stream(values).forEach(value -> builder.addParameter(name, value)));
            return builder.build().toString();
        });
    }
}
