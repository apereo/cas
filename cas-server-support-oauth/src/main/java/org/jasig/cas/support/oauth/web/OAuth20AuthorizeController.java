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
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This controller is in charge of responding to the authorize
 * call in OAuth protocol. It stores the callback url and redirects user to the
 * login page with the callback service.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20AuthorizeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizeController.class);

    private final String loginUrl;

    private final ServicesManager servicesManager;

    /**
     * Instantiates a new o auth20 authorize controller.
     *
     * @param servicesManager the services manager
     * @param loginUrl the login url
     */
    public OAuth20AuthorizeController(final ServicesManager servicesManager, final String loginUrl) {
        this.servicesManager = servicesManager;
        this.loginUrl = loginUrl;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        LOGGER.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String state = request.getParameter(OAuthConstants.STATE);
        LOGGER.debug("{} : {}", OAuthConstants.STATE, state);

        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            LOGGER.error("Missing {}", OAuthConstants.CLIENT_ID);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            LOGGER.error("Missing {}", OAuthConstants.REDIRECT_URI);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        if (service == null) {
            LOGGER.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            LOGGER.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return new ModelAndView(OAuthConstants.ERROR_VIEW);
        }

        // keep info in session
        final HttpSession session = request.getSession();
        session.setAttribute(OAuthConstants.OAUTH20_CALLBACKURL, redirectUri);
        session.setAttribute(OAuthConstants.OAUTH20_SERVICE_NAME, service.getName());
        session.setAttribute(OAuthConstants.BYPASS_APPROVAL_PROMPT, service.isBypassApprovalPrompt());
        session.setAttribute(OAuthConstants.OAUTH20_STATE, state);

        final String callbackAuthorizeUrl = request.getRequestURL().toString()
                .replace("/" + OAuthConstants.AUTHORIZE_URL, "/" + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        LOGGER.debug("{} : {}", OAuthConstants.CALLBACK_AUTHORIZE_URL, callbackAuthorizeUrl);

        final String loginUrlWithService = OAuthUtils.addParameter(loginUrl, OAuthConstants.SERVICE,
                callbackAuthorizeUrl);
        LOGGER.debug("loginUrlWithService : {}", loginUrlWithService);
        return OAuthUtils.redirectTo(loginUrlWithService);
    }
}
