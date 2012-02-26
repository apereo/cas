/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.support.oauth.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.support.oauth.OAuthUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This controller is the main entry point for OAuth version 2.0 wrapping in CAS, should be mapped to something like /oauth2.0/*. Dispatch
 * request to specific controllers : authorize, accessToken...
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20WrapperController extends BaseOAuthWrapperController implements InitializingBean {
    
    private AbstractController authorizeController;
    
    private AbstractController callbackAuthorizeController;
    
    private AbstractController accessTokenController;
    
    private AbstractController profileController;
    
    public void afterPropertiesSet() throws Exception {
        authorizeController = new OAuth20AuthorizeController(servicesManager, loginUrl);
        callbackAuthorizeController = new OAuth20CallbackAuthorizeController();
        accessTokenController = new OAuth20AccessTokenController(servicesManager, ticketRegistry, timeout);
        profileController = new OAuth20ProfileController(ticketRegistry);
    }
    
    @Override
    protected ModelAndView internalHandleRequest(String method, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        
        // authorize
        if ("authorize".equals(method)) {
            
            return authorizeController.handleRequest(request, response);
        }
        
        // callback on authorize
        else if ("callbackAuthorize".equals(method)) {
            
            return callbackAuthorizeController.handleRequest(request, response);
        }
        
        // get access token
        else if ("accessToken".equals(method)) {
            
            return accessTokenController.handleRequest(request, response);
        }
        
        // get profile
        else if ("profile".equals(method)) {
            
            return profileController.handleRequest(request, response);
        }
        
        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeText(response, "Unknown method : " + method);
        return null;
    }
}
