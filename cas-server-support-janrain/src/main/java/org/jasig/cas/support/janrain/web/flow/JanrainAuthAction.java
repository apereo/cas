/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jasig.cas.support.janrain.web.flow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;

import org.jasig.cas.support.janrain.authentication.principal.JanrainCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class represents an action in the webflow to retrieve user information from the Janrain Engage service on the callback url which is the 
 * webflow url (/login). The oauth_provider and the other OAuth parameters are expected after OAuth authentication. Providers are defined by configuration. The
 * service is stored and retrieved from web session after OAuth authentication.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class JanrainAuthAction extends AbstractAction {
    
    private static final Logger logger = LoggerFactory.getLogger(JanrainAuthAction.class);
    
    private static final String TOKEN_PARAMETER = "token";
    
    @NotNull 
    private CentralAuthenticationService centralAuthenticationService;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        HttpSession session = request.getSession();
        
        // get token value
        String tokenValue = request.getParameter(TOKEN_PARAMETER);
        
        // it's an authentication
        if (StringUtils.isNotBlank(tokenValue)) {
            
           // UserDataResponse userDataResponse = engageService.authInfo(tokenValue);
            //logger.debug("identifier : {}", userDataResponse.getProfile().getIdentifier());

            // get credential
            @SuppressWarnings("unchecked")
            JanrainCredentials credential = new JanrainCredentials(tokenValue);
            logger.debug("credential : {}", credential);
            
            // retrieve service from session and put it into webflow
            Service service = (Service) session.getAttribute("service");
            context.getFlowScope().put("service", service);
            
            // create credentials
           // Credentials credentials = new JanrainCredentials(credential);
            
            try {
                WebUtils.putTicketGrantingTicketInRequestScope(context, this.centralAuthenticationService
                    .createTicketGrantingTicket(credential));
                return success();
            } catch (final TicketException e) {
                return error();
            }
        } else {
            // no authentication : go to login page
            
            // put service in session from flow scope
            Service service = (Service) context.getFlowScope().get("service");
            session.setAttribute("service", service);
            
        }
        
        return error();
    }
    
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
