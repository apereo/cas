package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class OAuth20UserProfileEndpointController extends BaseOAuth20Controller {
    private final ResponseEntity<String> expiredAccessTokenResponseEntity;

    public OAuth20UserProfileEndpointController(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
        this.expiredAccessTokenResponseEntity = buildUnauthorizedResponseEntity(OAuth20Constants.EXPIRED_ACCESS_TOKEN);
    }

    /**
     * Build unauthorized response entity.
     *
     * @param code the code
     * @return the response entity
     */
    protected static ResponseEntity buildUnauthorizedResponseEntity(final String code) {
        val map = new LinkedMultiValueMap<String, String>(1);
        map.add(OAuth20Constants.ERROR, code);
        val value = OAuth20Utils.toJson(map);
        return new ResponseEntity<>(value, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle post request response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.PROFILE_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handlePostRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response) throws Exception {
        return handleGetRequest(request, response);
    }

    /**
     * Handle request internal response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.PROFILE_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleGetRequest(final HttpServletRequest request,
                                                   final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val accessToken = getAccessTokenFromRequest(request);
        if (StringUtils.isBlank(accessToken)) {
            LOGGER.error("Missing [{}] from the request", OAuth20Constants.ACCESS_TOKEN);
            return buildUnauthorizedResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN);
        }
        val accessTokenTicket = getOAuthConfigurationContext().getTicketRegistry()
            .getTicket(accessToken, OAuth20AccessToken.class);

        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            LOGGER.error("Access token [{}] cannot be found in the ticket registry or has expired.", accessToken);
            if (accessTokenTicket != null) {
                getOAuthConfigurationContext().getTicketRegistry().deleteTicket(accessTokenTicket);
            }
            return expiredAccessTokenResponseEntity;
        }
        updateAccessTokenUsage(accessTokenTicket);

        val map = getOAuthConfigurationContext().getUserProfileDataCreator().createFrom(accessTokenTicket, context);
        return getOAuthConfigurationContext().getUserProfileViewRenderer().render(map, accessTokenTicket, response);
    }

    /**
     * Update the access token in the registry.
     *
     * @param accessTokenTicket the access token
     */
    protected void updateAccessTokenUsage(final OAuth20AccessToken accessTokenTicket) {
        val accessTokenState = TicketState.class.cast(accessTokenTicket);
        accessTokenState.update();
        if (accessTokenTicket.isExpired()) {
            getOAuthConfigurationContext().getTicketRegistry().deleteTicket(accessTokenTicket.getId());
        } else {
            getOAuthConfigurationContext().getTicketRegistry().updateTicket(accessTokenTicket);
        }
    }

    /**
     * Gets access token from request.
     *
     * @param request the request
     * @return the access token from request
     */
    protected String getAccessTokenFromRequest(final HttpServletRequest request) {
        var accessToken = request.getParameter(OAuth20Constants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            val authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader) && authHeader.toLowerCase()
                .startsWith(OAuth20Constants.TOKEN_TYPE_BEARER.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuth20Constants.TOKEN_TYPE_BEARER.length() + 1);
            }
        }
        LOGGER.debug("[{}]: [{}]", OAuth20Constants.ACCESS_TOKEN, accessToken);
        return extractAccessTokenFrom(accessToken);
    }
}
