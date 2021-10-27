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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This controller returns an access token which is the CAS granting ticket according to the service and code (service ticket) given.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20AccessTokenController extends AbstractController {
    
    private static Logger log = LoggerFactory.getLogger(OAuth20AccessTokenController.class);
    
    private final ServicesManager servicesManager;
    
    private final TicketRegistry ticketRegistry;
    
    private final long timeout;
    
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
        log.debug("redirect_uri : {}", redirectUri);
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        log.debug("clientId : {}", clientId);
        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        log.debug("clientSecret : {}", clientSecret);
        final String code = request.getParameter(OAuthConstants.CODE);
        log.debug("code : {}", clientSecret);
        
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            log.error("missing clientId");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            log.error("missing redirectUri");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            log.error("missing clientSecret");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            log.error("missing code");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        
        // name of the CAS service
        final Collection<RegisteredService> services = servicesManager.getAllServices();
        RegisteredService service = null;
        for (final RegisteredService aService : services) {
            if (StringUtils.equals(aService.getName(), clientId)) {
                service = aService;
                break;
            }
        }
        if (service == null) {
            log.error("Unknown clientId : {}", clientId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        
        final String serviceId = service.getServiceId();
        // redirectUri should start with serviceId
        if (!StringUtils.startsWith(redirectUri, serviceId)) {
            log.error("Unsupported redirectUri : {} for serviceId : {}", redirectUri, serviceId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        
        // description of the service should be the secret
        final String serviceDescription = service.getDescription();
        if (!StringUtils.equals(serviceDescription, clientSecret)) {
            log.error("Wrong client secret : {} for service description : {}", clientSecret, serviceDescription);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 400);
        }
        
        final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(code);
        // service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            log.error("Code expired : {}", code);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, 400);
        }
        final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
        // remove service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());
        
        response.setContentType("text/plain");
        final int expires = (int) (timeout - ((System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()) / 1000));
        final String text = "access_token=" + ticketGrantingTicket.getId() + "&expires=" + expires;
        log.debug("text : {}", text);
        return OAuthUtils.writeText(response, text, 200);
    }
    
    static void setLogger(final Logger aLogger) {
        log = aLogger;
    }
}
