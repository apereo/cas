package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.util.CollectionUtils;
import org.pac4j.core.context.HttpConstants;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Controller("profileController")
public class OAuth20ProfileController extends BaseOAuthWrapperController {

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    /**
     * Handle request internal response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @RequestMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.PROFILE_URL,
            produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<String> handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws
            Exception {
        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader)
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        logger.debug("{}: {}", OAuthConstants.ACCESS_TOKEN, accessToken);

        if (StringUtils.isBlank(accessToken)) {
            logger.error("Missing {}", OAuthConstants.ACCESS_TOKEN);
            final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(1);
            map.add(OAuthConstants.ERROR, OAuthConstants.MISSING_ACCESS_TOKEN);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        final AccessToken accessTokenTicket = this.ticketRegistry.getTicket(accessToken, AccessToken.class);
        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            logger.error("Expired access token: {}", OAuthConstants.ACCESS_TOKEN);
            final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(1);
            map.add(OAuthConstants.ERROR, OAuthConstants.EXPIRED_ACCESS_TOKEN);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        final LinkedMultiValueMap<String, String> map =
                writeOutProfileResponse(accessTokenTicket.getAuthentication().getPrincipal());
        final String value = new ObjectMapper()
                            .writer()
                            .withDefaultPrettyPrinter()
                            .writeValueAsString(map);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    /**
     * Write out profile response.
     *
     * @param principal the principal
     * @return the linked multi value map
     * @throws IOException the io exception
     */
    protected LinkedMultiValueMap<String, String> writeOutProfileResponse(final Principal principal) throws IOException {
        final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(ID, principal.getId());

        final Map<String, Object> attributes = principal.getAttributes();
        for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
            final Set<Object> values = CollectionUtils.convertValueToCollection(entry.getValue());
            values.stream().forEach(value -> map.add(entry.getKey(), value.toString()));
        }
        return map;
    }

}
