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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * This controller returns an access token which is the CAS
 * granting ticket according to the service and code (service ticket) given.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20AccessTokenController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AccessTokenController.class);

    private final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;

    private final long timeout;

    /**
     * Instantiates a new o auth20 access token controller.
     *
     * @param servicesManager the services manager
     * @param ticketRegistry the ticket registry
     * @param timeout the timeout
     */
    public OAuth20AccessTokenController(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
            final long timeout) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.timeout = timeout;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

        final String code = request.getParameter(OAuthConstants.CODE);
        LOGGER.debug("{} : {}", OAuthConstants.CODE, code);

        final boolean isVerified = verifyAccessTokenRequest(response, redirectUri, clientId, clientSecret, code);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(code);
        // service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            LOGGER.error("Code expired : {}", code);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
        final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
        // remove service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());

        response.setContentType("text/plain");
        final int expires = (int) (timeout - TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()));

        final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, ticketGrantingTicket.getId(),
                                                    OAuthConstants.EXPIRES, expires);
        LOGGER.debug("text : {}", text);
        return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
    }

    /**
     * Verify access token request by reviewing the values of
     * client id, redirect uri, client secret, code, etc.
     *
     * @param response the response
     * @param redirectUri the redirect uri
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param code the code
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletResponse response, final String redirectUri,
                                             final String clientId, final String clientSecret, final String code) {

        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            return false;
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            return false;
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            return false;
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            LOGGER.error("Missing {}", OAuthConstants.CODE);
            return false;
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return false;
        }

        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            LOGGER.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return false;
        }

        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            LOGGER.error("Wrong client secret for service {}", service);
            return false;
        }
        return true;
    }
}
