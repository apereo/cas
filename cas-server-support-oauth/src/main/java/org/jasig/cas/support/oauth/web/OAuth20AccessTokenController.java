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
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
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
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth20AccessTokenController.class);
    
    private ServicesManager servicesManager;
    
    private TicketRegistry ticketRegistry;
    
    private long timeout;
    
    public OAuth20AccessTokenController(ServicesManager servicesManager, TicketRegistry ticketRegistry, long timeout) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.timeout = timeout;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        
        String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        logger.debug("redirect_uri : {}", redirectUri);
        String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        logger.debug("clientId : {}", clientId);
        String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        logger.debug("clientSecret : {}", clientSecret);
        String code = request.getParameter(OAuthConstants.CODE);
        logger.debug("code : {}", clientSecret);
        
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            logger.error("missing clientId");
            return OAuthUtils.writeTextError(response, "missing_clientId");
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            logger.error("missing redirectUri");
            return OAuthUtils.writeTextError(response, "missing_redirectUri");
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            logger.error("missing clientSecret");
            return OAuthUtils.writeTextError(response, "missing_clientSecret");
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            logger.error("missing code");
            return OAuthUtils.writeTextError(response, "missing_code");
        }
        
        // name of the CAS service
        Collection<RegisteredService> services = servicesManager.getAllServices();
        RegisteredService service = null;
        for (RegisteredService aService : services) {
            if (StringUtils.equals(aService.getName(), clientId)) {
                service = aService;
                break;
            }
        }
        if (service == null) {
            logger.error("Unknown clientId : {}", clientId);
            return OAuthUtils.writeTextError(response, "unknow_clientId");
        }
        
        String serviceId = service.getServiceId();
        // redirectUri should start with serviceId
        if (!StringUtils.startsWith(redirectUri, serviceId)) {
            logger.error("Unsupported redirectUri : {} for serviceId : {}", redirectUri, serviceId);
            return OAuthUtils.writeTextError(response, "unsupported_redirectUri");
        }
        
        // description of the service should be the secret
        String serviceDescription = service.getDescription();
        if (!StringUtils.equals(serviceDescription, clientSecret)) {
            logger.error("Wrong client secret : {} for service description : {}", clientSecret, serviceDescription);
            return OAuthUtils.writeTextError(response, "unknown_clientId");
        }
        
        ServiceTicketImpl serviceTicket = (ServiceTicketImpl) ticketRegistry.getTicket(code);
        // service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            logger.error("Code expired : {}", code);
            return OAuthUtils.writeTextError(response, "code_expired");
        }
        TicketGrantingTicketImpl ticketGrantingTicketImpl = (TicketGrantingTicketImpl) serviceTicket
            .getGrantingTicket();
        // remove service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());
        
        int expires = (int) (timeout - ((System.currentTimeMillis() - ticketGrantingTicketImpl.getLastTimeUsed()) / 1000));
        String text = "access_token=" + ticketGrantingTicketImpl.getId() + "&expires=" + expires;
        logger.debug("text : {}", text);
        return OAuthUtils.writeText(response, text);
    }
}
