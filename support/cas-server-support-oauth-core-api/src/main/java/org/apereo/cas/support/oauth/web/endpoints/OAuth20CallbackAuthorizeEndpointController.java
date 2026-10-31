package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The callback of the login handshake of the authorization endpoint. CAS sends the browser here with a service ticket
 * for the callback URL; the ticket is validated for the URL it was issued for, the session is renewed when the session
 * store can renew it (a session replicated through the ticket registry keeps its identifier), the profile of the user is
 * kept in it for the client of the request, and the request saved before the login is resumed. Without a
 * saved request the browser goes to the redirect URI of the client, when it is one the client registered, or to
 * {@value #DEFAULT_URL}. A ticket that does not validate is answered with the service error view and {@code 401}.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@Tag(name = "OAuth")
public class OAuth20CallbackAuthorizeEndpointController extends BaseOAuth20Controller<OAuth20ConfigurationContext> {
    /**
     * Where the browser goes without a saved request or a registered redirect URI.
     */
    public static final String DEFAULT_URL = "/";

    public OAuth20CallbackAuthorizeEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL)
    @Operation(summary = "Handle OAuth callback authorize request", hidden = true)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        ensureSessionReplicationIsAutoconfiguredIfNeedBe(request);
        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isNotBlank(ticket) && !authenticate(request, response, ticket)) {
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.UNAUTHORIZED);
        }
        val url = restoreRequestedUrl(request, response);
        val profile = OAuth20Utils.findAuthenticatedUserProfile(request, response, getConfigurationContext().getSessionStore()).orElse(null);
        LOGGER.trace("OAuth callback URL is [{}]", url);
        return getConfigurationContext().getCallbackAuthorizeViewResolver().resolve(request, response, profile, url);
    }

    /**
     * Validate the ticket for the callback URL it was issued for, renew the session when the session store can, and keep the
     * profile of the user in it, recording the client of the request as its {@code oauthClientId} attribute. Whether the
     * ticket comes from a new login is kept for the request under {@link OAuth20Constants#REQUEST_ATTRIBUTE_NEW_LOGIN}.
     *
     * @param request  the request
     * @param response the response
     * @param ticket   the service ticket
     * @return whether the ticket validated
     */
    protected boolean authenticate(final HttpServletRequest request, final HttpServletResponse response, final String ticket) {
        val context = getConfigurationContext();
        val service = context.getCasCallbackUrlResolver().computeServiceForTicket(request);
        try {
            val validatedTicket = context.getCasTicketValidator().validate(ticket, service);
            val profile = validatedTicket.profile();
            val clientId = OAuth20Utils.resolveClientIdFromRequest(request, context.getServicesManager(), context.getRequestParameterResolver());
            if (StringUtils.isNotBlank(clientId)) {
                profile.addAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID, clientId);
            }
            context.getSessionStore().renewSession(request);
            context.getSessionStore().set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
            request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
            request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_NEW_LOGIN, validatedTicket.fromNewLogin());
            LOGGER.debug("Service ticket [{}] for [{}] authenticated [{}]", ticket, service, profile.getId());
            return true;
        } catch (final Throwable e) {
            LOGGER.warn("Unable to validate service ticket [{}] for [{}]: [{}]", ticket, service, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return false;
        }
    }

    /**
     * The request saved before the login, which is removed from the session, or else the redirect URI of the client when
     * it is one the client registered, or else {@value #DEFAULT_URL}.
     *
     * @param request  the request
     * @param response the response
     * @return the url to go to
     */
    protected String restoreRequestedUrl(final HttpServletRequest request, final HttpServletResponse response) {
        val sessionStore = getConfigurationContext().getSessionStore();
        val requestedUrl = sessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).map(String::valueOf);
        if (requestedUrl.isPresent()) {
            sessionStore.set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL, null);
            return requestedUrl.get();
        }
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        val redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        if (StringUtils.isNoneBlank(clientId, redirectUri)) {
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
            if (registeredService != null && registeredService.matches(redirectUri)) {
                return redirectUri;
            }
        }
        return DEFAULT_URL;
    }
}
