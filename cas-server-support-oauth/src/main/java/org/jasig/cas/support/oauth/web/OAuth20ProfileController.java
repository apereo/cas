package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token (CAS granting
 * ticket).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("profileController")
public final class OAuth20ProfileController extends BaseOAuthWrapperController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    /**
     * Instantiates a new o auth20 profile controller.
     *
     */
    public OAuth20ProfileController() { }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {

        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) 
                    && authHeader.toLowerCase().startsWith(OAuthConstants.BEARER_TOKEN.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuthConstants.BEARER_TOKEN.length() + 1);
            }
        }
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessToken);

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
            // get ticket granting ticket
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(accessToken);
            if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
                LOGGER.error("expired accessToken : {}", accessToken);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("error", OAuthConstants.EXPIRED_ACCESS_TOKEN);
                jsonGenerator.writeEndObject();
                return null;
            }
            // generate profile : identifier + attributes
            final Principal principal = ticketGrantingTicket.getAuthentication().getPrincipal();
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(ID, principal.getId());
            jsonGenerator.writeArrayFieldStart(ATTRIBUTES);
            final Map<String, Object> attributes = principal.getAttributes();
            for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            return null;
        } finally {
            response.flushBuffer();
        }
    }

}
