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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This controller is in charge of responding to the authorize call in OAuth protocol. It stores the callback url and redirects user to the
 * login page with the callback service.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20AuthorizeController extends AbstractController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth20AuthorizeController.class);
    
    private final String loginUrl;
    
    private final ServicesManager servicesManager;
    
    public OAuth20AuthorizeController(final ServicesManager servicesManager, final String loginUrl) {
        this.servicesManager = servicesManager;
        this.loginUrl = loginUrl;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        
        String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        logger.debug("redirect_uri : {}", redirectUri);
        String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        logger.debug("clientId : {}", clientId);
        String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);
        logger.debug("clientSecret : {}", clientSecret);
        
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            logger.error("missing clientId");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 200);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            logger.error("missing redirectUri");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 200);
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
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 200);
        }
        
        String serviceId = service.getServiceId();
        // redirectUri should start with serviceId
        if (!StringUtils.startsWith(redirectUri, serviceId)) {
            logger.error("Unsupported redirectUri : {} for serviceId : {}", redirectUri, serviceId);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, 200);
        }
        
        // keep redirectUri in session
        HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.OAUTH20_CALLBACKURL, redirectUri);
        
        String callbackAuthorizeUrl = request.getRequestURL().toString()
            .replace("/" + OAuthConstants.AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        logger.debug("callbackAuthorizeUrl : {}", callbackAuthorizeUrl);
        
        String loginUrlWithService = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE, callbackAuthorizeUrl);
        logger.debug("loginUrlWithService : {}", loginUrlWithService);
        return OAuthUtils.redirectTo(loginUrlWithService);
    }
}
