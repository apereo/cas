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

import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller is the main entry point for OAuth version 2.0
 * wrapping in CAS, should be mapped to something like /oauth2.0/*. Dispatch
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

    @Override
    public void afterPropertiesSet() throws Exception {
        authorizeController = new OAuth20AuthorizeController(servicesManager, loginUrl);
        callbackAuthorizeController = new OAuth20CallbackAuthorizeController();
        accessTokenController = new OAuth20AccessTokenController(servicesManager, this.centralAuthenticationService,
                timeout);
        profileController = new OAuth20ProfileController(this.centralAuthenticationService);
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        // authorize
        if (OAuthConstants.AUTHORIZE_URL.equals(method)) {
            return authorizeController.handleRequest(request, response);
        }
        // callback on authorize
        if (OAuthConstants.CALLBACK_AUTHORIZE_URL.equals(method)) {
            return callbackAuthorizeController.handleRequest(request, response);
        }
        //get access token
        if (OAuthConstants.ACCESS_TOKEN_URL.equals(method)) {
            return accessTokenController.handleRequest(request, response);
        }
        // get profile
        if (OAuthConstants.PROFILE_URL.equals(method)) {
            return profileController.handleRequest(request, response);
        }

        // else error
        logger.error("Unknown method : {}", method);
        OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_OK);
        return null;
    }
}
