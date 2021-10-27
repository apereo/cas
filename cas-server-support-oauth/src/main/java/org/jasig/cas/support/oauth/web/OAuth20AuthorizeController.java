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
    
    private static Logger log = LoggerFactory.getLogger(OAuth20AuthorizeController.class);
    
    private final String loginUrl;
    
    private final ServicesManager servicesManager;
    
    public OAuth20AuthorizeController(final ServicesManager servicesManager, final String loginUrl) {
        this.servicesManager = servicesManager;
        this.loginUrl = loginUrl;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        log.debug("clientId : {}", clientId);
        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        log.debug("redirect_uri : {}", redirectUri);
        final String state = request.getParameter(OAuthConstants.STATE);
        log.debug("state : {}", state);
        
        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            log.error("missing clientId");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            log.error("missing redirectUri");
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
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
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        
        final String serviceId = service.getServiceId();
        // redirectUri should start with serviceId
        if (!StringUtils.startsWith(redirectUri, serviceId)) {
            log.error("Unsupported redirectUri : {} for serviceId : {}", redirectUri, serviceId);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        
        // keep info in session
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.OAUTH20_CALLBACKURL, redirectUri);
        session.setAttribute(OAuthConstants.OAUTH20_SERVICE_NAME, service.getTheme());
        session.setAttribute(OAuthConstants.OAUTH20_STATE, state);
        
        final String callbackAuthorizeUrl = request.getRequestURL().toString()
            .replace("/" + OAuthConstants.AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        log.debug("callbackAuthorizeUrl : {}", callbackAuthorizeUrl);
        
        final String loginUrlWithService = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE,
                                                                   callbackAuthorizeUrl);
        log.debug("loginUrlWithService : {}", loginUrlWithService);
        return OAuthUtils.redirectTo(loginUrlWithService);
    }
    
    static void setLogger(final Logger aLogger) {
        log = aLogger;
    }
}
