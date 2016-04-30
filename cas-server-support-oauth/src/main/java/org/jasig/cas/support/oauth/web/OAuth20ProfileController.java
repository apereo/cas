package org.jasig.cas.support.oauth.web;

import org.jasig.cas.support.oauth.OAuthConstants;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringUtils;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;

import org.pac4j.core.context.HttpConstants;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Component("profileController")
public class OAuth20ProfileController extends BaseOAuthWrapperController {

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";
    
    @RequestMapping(path=OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.PROFILE_URL)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader)
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        logger.debug("{}: {}", OAuthConstants.ACCESS_TOKEN, accessToken);

        try (final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(response.getWriter())) {
            response.setContentType("application/json");
            // accessToken is required
            if (StringUtils.isBlank(accessToken)) {
                logger.error("Missing {}", OAuthConstants.ACCESS_TOKEN);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(OAuthConstants.ERROR, OAuthConstants.MISSING_ACCESS_TOKEN);
                jsonGenerator.writeEndObject();
                return null;
            }
            try {

                final AccessToken accessTokenTicket = this.ticketRegistry.getTicket(accessToken, AccessToken.class);
                if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
                    logger.error("Expired access token: {}", OAuthConstants.ACCESS_TOKEN);
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(OAuthConstants.ERROR, OAuthConstants.EXPIRED_ACCESS_TOKEN);
                    jsonGenerator.writeEndObject();
                    return null;
                }

                writeOutProfileResponse(jsonGenerator, accessTokenTicket.getAuthentication().getPrincipal());
            } catch (final Exception e) {
                logger.error("Cannot JSONify profile", e);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("error", OAuthConstants.INVALID_REQUEST + ". " + e.getMessage());
                jsonGenerator.writeEndObject();
            }
            return null;
        } finally {
            response.flushBuffer();
        }
    }

    private void writeOutProfileResponse(final JsonGenerator jsonGenerator, final Principal principal) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(ID, principal.getId());
        jsonGenerator.writeArrayFieldStart(ATTRIBUTES);
        final Map<String, Object> attributes = principal.getAttributes();
        for (final String key : attributes.keySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField(key, attributes.get(key));
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
