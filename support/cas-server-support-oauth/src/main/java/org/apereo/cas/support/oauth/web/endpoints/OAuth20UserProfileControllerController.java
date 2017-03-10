package org.apereo.cas.support.oauth.web.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20UserProfileControllerController extends BaseOAuthWrapperController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20UserProfileControllerController.class);

    private static final String ID = "id";
    private static final String ATTRIBUTES = "attributes";

    public OAuth20UserProfileControllerController(final ServicesManager servicesManager,
                                                  final TicketRegistry ticketRegistry,
                                                  final OAuth20Validator validator,
                                                  final AccessTokenFactory accessTokenFactory,
                                                  final PrincipalFactory principalFactory,
                                                  final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                  final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                  final CasConfigurationProperties casProperties) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties);
    }

    /**
     * Handle request internal response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader)
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        LOGGER.debug("[{}]: [{}]", OAuthConstants.ACCESS_TOKEN, accessToken);

        if (StringUtils.isBlank(accessToken)) {
            LOGGER.error("Missing [{}]", OAuthConstants.ACCESS_TOKEN);
            final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(1);
            map.add(OAuthConstants.ERROR, OAuthConstants.MISSING_ACCESS_TOKEN);
            final String value = OAuthUtils.jsonify(map);
            return new ResponseEntity<>(value, HttpStatus.UNAUTHORIZED);
        }

        final AccessToken accessTokenTicket = getTicketRegistry().getTicket(accessToken, AccessToken.class);
        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            LOGGER.error("Expired access token: [{}]", OAuthConstants.ACCESS_TOKEN);
            final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(1);
            map.add(OAuthConstants.ERROR, OAuthConstants.EXPIRED_ACCESS_TOKEN);
            final String value = OAuthUtils.jsonify(map);
            return new ResponseEntity<>(value, HttpStatus.UNAUTHORIZED);
        }

        final Map<String, Object> map = writeOutProfileResponse(accessTokenTicket.getAuthentication(),
                accessTokenTicket.getAuthentication().getPrincipal());
        final String value = OAuthUtils.jsonify(map);
        LOGGER.debug("Final user profile is [{}]", value);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    /**
     * Write out profile response.
     *
     * @param authentication the authentication
     * @param principal      the principal
     * @return the linked multi value map
     * @throws IOException the io exception
     */
    protected Map<String, Object> writeOutProfileResponse(final Authentication authentication,
                                                          final Principal principal) throws IOException {
        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", principal);
        final Map<String, Object> map = new HashMap<>();
        map.put(ID, principal.getId());
        map.put(ATTRIBUTES, principal.getAttributes());
        return map;
    }

}
