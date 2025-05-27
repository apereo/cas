package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.pac4j.core.context.HttpConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "OAuth")
public class OAuth20UserProfileEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    public OAuth20UserProfileEndpointController(final T configurationContext) {
        super(configurationContext);
    }

    protected static ResponseEntity buildUnauthorizedResponseEntity(final String code) {
        val map = OAuth20Utils.getErrorResponseBody(code, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
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
    @Operation(summary = "Handle user profile request",
        parameters = @Parameter(name = "access_token", in = ParameterIn.QUERY, required = true, description = "Access token"))
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
    @Operation(summary = "Handle user profile request",
        parameters = @Parameter(name = "access_token", in = ParameterIn.QUERY, required = true, description = "Access token"))
    public ResponseEntity<String> handleGetRequest(final HttpServletRequest request,
                                                   final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        val accessTokenResult = FunctionUtils.doAndHandle(() -> getAccessTokenFromRequest(request));
        if (accessTokenResult == null) {
            LOGGER.error("Unable to get the access token from the request");
            return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_REQUEST);
        }

        val decodedAccessTokenId = accessTokenResult.getValue();
        if (StringUtils.isBlank(decodedAccessTokenId)) {
            LOGGER.error("Missing required parameter [{}] from the request", OAuth20Constants.ACCESS_TOKEN);
            return buildUnauthorizedResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN);
        }

        val accessTokenTicket = FunctionUtils.doAndHandle(() -> {
            val decodedToken = getConfigurationContext().getTicketRegistry().getTicket(decodedAccessTokenId, OAuth20AccessToken.class);
            return decodedToken == null || decodedToken.isExpired() ? null : decodedToken;
        });
        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            LOGGER.error("Access token [{}] cannot be found in the ticket registry or has expired.", decodedAccessTokenId);
            return buildUnauthorizedResponseEntity(OAuth20Constants.EXPIRED_ACCESS_TOKEN);
        }
        LoggingUtils.protocolMessage("OAuth/OpenID Connect User Profile Request",
            Map.of("Access Token", decodedAccessTokenId, "Client ID", accessTokenTicket.getClientId()));

        try {
            validateAccessToken(accessTokenResult.getKey(), accessTokenTicket, request, response);
            updateAccessTokenUsage(accessTokenTicket);
            val map = getConfigurationContext().getUserProfileDataCreator().createFrom(accessTokenTicket);
            return getConfigurationContext().getUserProfileViewRenderer().render(map, accessTokenTicket, response);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_REQUEST);
        }
    }

    protected void validateAccessToken(final String accessTokenId, final OAuth20AccessToken accessToken,
                                       final HttpServletRequest request, final HttpServletResponse response) {
    }

    protected void updateAccessTokenUsage(final OAuth20AccessToken accessTokenTicket) throws Exception {
        if (!accessTokenTicket.isStateless()) {
            accessTokenTicket.update();
            val ticketRegistry = getConfigurationContext().getTicketRegistry();
            if (accessTokenTicket.isExpired()) {
                ticketRegistry.deleteTicket(accessTokenTicket.getId());
            } else {
                ticketRegistry.updateTicket(accessTokenTicket);
                FunctionUtils.doAndHandle(__ -> {
                    val tgt = ticketRegistry.getTicket(accessTokenTicket.getTicketGrantingTicket().getId(), TicketGrantingTicket.class);
                    ticketRegistry.updateTicket(tgt.update());
                });
            }
        }
    }

    protected Pair<String, String> getAccessTokenFromRequest(final HttpServletRequest request) {
        var accessToken = StringUtils.defaultIfBlank(
            request.getParameter(OAuth20Constants.ACCESS_TOKEN),
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
