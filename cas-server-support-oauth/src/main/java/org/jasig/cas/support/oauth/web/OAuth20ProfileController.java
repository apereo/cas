package org.jasig.cas.support.oauth.web;

import org.jasig.cas.support.oauth.OAuthConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("profileController")
public final class OAuth20ProfileController extends BaseOAuthWrapperController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    @Autowired
    @Qualifier("accessTokenJwtAuthenticator")
    private JwtAuthenticator accessTokenJwtAuthenticator;

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    /**
     * Instantiates a new o auth20 profile controller.
     */
    public OAuth20ProfileController() {
    }

    /**
     * Ensure the encryption secret has been set.
     */
    @PostConstruct
    public void postConstruct() {
        CommonHelper.assertNotNull("encryptionSecret", accessTokenJwtAuthenticator.getEncryptionSecret());
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {

        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader)
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        LOGGER.debug("{}: {}", OAuthConstants.ACCESS_TOKEN, accessToken);

        try (final JsonGenerator jsonGenerator = this.jsonFactory.createJsonGenerator(response.getWriter())) {
            response.setContentType("application/json");
            // accessToken is required
            if (StringUtils.isBlank(accessToken)) {
                LOGGER.error("Missing {}", OAuthConstants.ACCESS_TOKEN);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("error", OAuthConstants.MISSING_ACCESS_TOKEN);
                jsonGenerator.writeEndObject();
                return null;
            }
            try {

                final UserProfile profile = this.accessTokenJwtAuthenticator.validateToken(accessToken);
                final ZonedDateTime expirationDate = (ZonedDateTime) profile.getAttribute(JwtConstants.EXPIRATION_TIME);
                final ZonedDateTime now = ZonedDateTime.now();
                if (expirationDate == null || expirationDate.isBefore(now)) {
                    LOGGER.error("Expired access token: {}", OAuthConstants.ACCESS_TOKEN);
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("error", OAuthConstants.EXPIRED_ACCESS_TOKEN);
                    jsonGenerator.writeEndObject();
                    return null;
                }

                writeOutProfileResponse(jsonGenerator, profile);
            } catch (final Exception e) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("error", OAuthConstants.INVALID_REQUEST + ". " + e.getMessage());
                jsonGenerator.writeEndObject();
            }
            return null;
        } finally {
            response.flushBuffer();
        }
    }

    private void writeOutProfileResponse(final JsonGenerator jsonGenerator, final UserProfile profile) throws IOException {
        final String id = profile.getId();
        final Map<String, Object> attributes = new HashMap<>(profile.getAttributes());
        attributes.remove(JwtConstants.SUBJECT);
        attributes.remove(JwtConstants.ISSUE_TIME);
        attributes.remove(JwtConstants.AUDIENCE);
        attributes.remove(JwtConstants.EXPIRATION_TIME);
        attributes.remove(JwtConstants.ISSUER);

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(ID, id);
        jsonGenerator.writeArrayFieldStart(ATTRIBUTES);
        for (final String key : attributes.keySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField(key, attributes.get(key));
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
