/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token (CAS granting
 * ticket).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20ProfileController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(OAuth20ProfileController.class);

    private static final String ID = "id";

    private static final String ATTRIBUTES = "attributes";

    private final TicketRegistry ticketRegistry;

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    public OAuth20ProfileController(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        LOGGER.debug("{} : {}", OAuthConstants.ACCESS_TOKEN, accessToken);

        final JsonGenerator jsonGenerator = this.jsonFactory.createJsonGenerator(response.getWriter());

        try {
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
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry
                    .getTicket(accessToken);
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
            for (final String key : attributes.keySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(key, attributes.get(key));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            return null;
        } finally {
            IOUtils.closeQuietly(jsonGenerator);
            response.flushBuffer();
        }
    }
}
