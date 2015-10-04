/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

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
public final class OAuth20ProfileController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiates a new o auth20 profile controller.
     *
     * @param centralAuthenticationService the CAS instance
     */
    public OAuth20ProfileController(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
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

        try (final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(response.getWriter())) {
            response.setContentType("application/json");

            if (checkPresenseOfAccessToken(accessToken, jsonGenerator)) {
                return null;
            }

            final TicketGrantingTicket ticketGrantingTicket =
                    checkPresenseOfTicketGrantingTicket(accessToken, jsonGenerator);
            if (ticketGrantingTicket == null) {
                return null;
            }

            generateUserProfile(jsonGenerator, ticketGrantingTicket);
            return null;
        } finally {
            try {
                response.flushBuffer();
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Generate user profile.
     *
     * @param jsonGenerator the json generator
     * @param ticketGrantingTicket the ticket granting ticket
     * @throws IOException the iO exception
     */
    private void generateUserProfile(final JsonGenerator jsonGenerator,
                                     final TicketGrantingTicket ticketGrantingTicket) throws IOException {

        final Principal principal = ticketGrantingTicket.getAuthentication().getPrincipal();
        LOGGER.debug("Generating OAuth user profile for {}", principal.getId());
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(ID, principal.getId());
        jsonGenerator.writeArrayFieldStart(ATTRIBUTES);
        final Map<String, Object> attributes = principal.getAttributes();
        for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
            jsonGenerator.writeStartObject();
            LOGGER.debug("Added attribute [{}] to OAuth user profile: [{}]", entry.getKey(), entry.getValue());
            jsonGenerator.writeObjectField(entry.getKey(), entry.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        LOGGER.debug("Generated OAuth user profile successfully.", principal.getId());
    }

    /**
     * Check presense of ticket granting ticket.
     *
     * @param accessToken the access token
     * @param jsonGenerator the json generator
     * @return the ticket granting ticket
     * @throws IOException the iO exception
     */
    private TicketGrantingTicket checkPresenseOfTicketGrantingTicket(final String accessToken,
                                                                    final JsonGenerator jsonGenerator)
                                                                    throws IOException {
        TicketGrantingTicket ticketGrantingTicket = null;
        try {
            ticketGrantingTicket = this.centralAuthenticationService.getTicket(accessToken, TicketGrantingTicket.class);
        } catch (final InvalidTicketException e) {
            LOGGER.error("Invalid accessToken: {}. {}", e.getMessage(), accessToken);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("error", OAuthConstants.EXPIRED_ACCESS_TOKEN);
            jsonGenerator.writeEndObject();
        }
        return ticketGrantingTicket;
    }

    /**
     * Check presense of access token.
     *
     * @param accessToken the access token
     * @param jsonGenerator the json generator
     * @return the boolean
     * @throws IOException the iO exception
     */
    private boolean checkPresenseOfAccessToken(final String accessToken, final JsonGenerator jsonGenerator) throws IOException {
        if (StringUtils.isBlank(accessToken)) {
            LOGGER.error("Missing {}", OAuthConstants.ACCESS_TOKEN);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("error", OAuthConstants.MISSING_ACCESS_TOKEN);
            jsonGenerator.writeEndObject();
            return true;
        }
        return false;
    }
}
