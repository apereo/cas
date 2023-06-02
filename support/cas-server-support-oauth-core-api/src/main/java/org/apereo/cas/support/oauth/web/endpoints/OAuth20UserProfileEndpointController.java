package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class OAuth20UserProfileEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    public OAuth20UserProfileEndpointController(final T configurationContext) {
        super(configurationContext);
    }

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
        val accessTokenResult = getAccessTokenFromRequest(request);

        val decodedAccessTokenId = accessTokenResult.getValue();
        if (StringUtils.isBlank(decodedAccessTokenId)) {
            LOGGER.error("Missing required parameter [{}] from the request", OAuth20Constants.ACCESS_TOKEN);
            return buildUnauthorizedResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN);
        }

        val accessTokenTicket = FunctionUtils.doAndHandle(() -> {
            val state = getConfigurationContext().getTicketRegistry().getTicket(decodedAccessTokenId, OAuth20AccessToken.class);
            return state == null || state.isExpired() ? null : state;
        });
        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            LOGGER.error("Access token [{}] cannot be found in the ticket registry or has expired.", decodedAccessTokenId);
            return buildUnauthorizedResponseEntity(OAuth20Constants.EXPIRED_ACCESS_TOKEN);
        }
        LoggingUtils.protocolMessage("OAuth/OpenID Connect User Profile Request",
            Map.of("Access Token", decodedAccessTokenId, "Client ID", accessTokenTicket.getClientId()));
        
        try {
            validateAccessToken(accessTokenResult.getKey(), accessTokenTicket, request, response);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_REQUEST);
        }
        return FunctionUtils.doAndHandle(() -> {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(accessTokenTicket.getAuthentication());
            updateAccessTokenUsage(accessTokenTicket);
            val context = new JEEContext(request, response);
            val map = getConfigurationContext().getUserProfileDataCreator().createFrom(accessTokenTicket, context);
            return getConfigurationContext().getUserProfileViewRenderer().render(map, accessTokenTicket, response);
        },
            e -> buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_REQUEST)).get();
    }

    protected void validateAccessToken(final String accessTokenId, final OAuth20AccessToken accessToken,
                                       final HttpServletRequest request, final HttpServletResponse response) {
    }

    protected void updateAccessTokenUsage(final OAuth20AccessToken accessTokenTicket) throws Exception {
        accessTokenTicket.update();
        if (accessTokenTicket.isExpired()) {
            getConfigurationContext().getTicketRegistry().deleteTicket(accessTokenTicket.getId());
        } else {
            getConfigurationContext().getTicketRegistry().updateTicket(accessTokenTicket);
        }
    }

    protected Pair<String, String> getAccessTokenFromRequest(final HttpServletRequest request) {
        var accessToken = StringUtils.defaultIfBlank(request.getParameter(OAuth20Constants.ACCESS_TOKEN),
            request.getParameter(OAuth20Constants.TOKEN));
        if (StringUtils.isBlank(accessToken)) {
            val authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader) && authHeader.toLowerCase(Locale.ENGLISH)
                .startsWith(OAuth20Constants.TOKEN_TYPE_BEARER.toLowerCase(Locale.ENGLISH) + ' ')) {
                accessToken = authHeader.substring(OAuth20Constants.TOKEN_TYPE_BEARER.length() + 1);
            }
        }
        LOGGER.debug("[{}]: [{}]", OAuth20Constants.ACCESS_TOKEN, accessToken);
        return Pair.of(accessToken, extractAccessTokenFrom(accessToken));
    }
}
