package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.UnauthorizedServiceForPrincipalException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.Pair;
import org.pac4j.core.context.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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


    @Autowired
    @Qualifier("defaultAccessTokenGenerator")
    private AccessTokenGenerator accessTokenGenerator;

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    /**
     * Instantiates a new o auth20 profile controller.
     */
    public OAuth20ProfileController() {
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
            try {
                final Pair<String, Service> pair = this.accessTokenGenerator.degenerate(accessToken);
                accessToken = pair.getFirst();

                final TicketGrantingTicket ticketGrantingTicket = verifyAccessToken(accessToken, jsonGenerator);
                if (ticketGrantingTicket == null) {
                    return null;
                }

                final RegisteredService service = verifyRegisteredService(jsonGenerator, pair);
                if (service == null) {
                    return null;
                }

                final Principal principal = ticketGrantingTicket.getAuthentication().getPrincipal();
                if (!verifyPrincipalServiceAccess(jsonGenerator, service, principal)) {
                    return null;
                }

                writeOutProfileResponse(jsonGenerator, service, principal);
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

    private boolean verifyPrincipalServiceAccess(final JsonGenerator jsonGenerator, final RegisteredService service,
                                                 final Principal principal) throws IOException {
        if (!service.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principal.getId(), principal.getAttributes())) {
            logger.warn("Service [{}] is not authorized for use by [{}].", service.getServiceId(), principal);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("error", UnauthorizedServiceForPrincipalException.CODE_UNAUTHZ_SERVICE);
            jsonGenerator.writeEndObject();
            return false;
        }
        return true;
    }

    private RegisteredService verifyRegisteredService(final JsonGenerator jsonGenerator, final Pair<String, Service> pair)
            throws IOException {
        final RegisteredService service = this.servicesManager.findServiceBy(Long.parseLong(pair.getSecond().getId()));
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            logger.warn("Service {}] is not found in the registry or it is disabled.", service);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("error", OAuthConstants.INVALID_REQUEST);
            jsonGenerator.writeEndObject();
            return null;
        }
        return service;
    }

    private TicketGrantingTicket verifyAccessToken(final String accessToken, final JsonGenerator jsonGenerator) throws IOException {
        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(accessToken);
        if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
            LOGGER.error("expired accessToken : {}", accessToken);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("error", OAuthConstants.INVALID_REQUEST);
            jsonGenerator.writeEndObject();
            return null;
        }
        return ticketGrantingTicket;
    }

    private void writeOutProfileResponse(final JsonGenerator jsonGenerator, final RegisteredService service,
                                         final Principal principal) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(ID, principal.getId());
        jsonGenerator.writeArrayFieldStart(ATTRIBUTES);
        final Map<String, Object> attributes = service.getAttributeReleasePolicy().getAttributes(principal);
        for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

}
