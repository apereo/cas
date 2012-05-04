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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This controller is called after successful authentication and redirects user to the callback url of the OAuth application. A code is
 * added which is the service ticket retrieved from previous authentication.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuth20CallbackAuthorizeController extends AbstractController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth20CallbackAuthorizeController.class);
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // get CAS ticket
        String ticket = request.getParameter("ticket");
        logger.debug("ticket : {}", ticket);
        
        // retrieve callback url from session
        HttpSession session = request.getSession();
        String callbackUrl = (String) session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        logger.debug("callbackUrl : {}", callbackUrl);
        session.removeAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        
        // return to callback with code
        String callbackUrlWithCode = OAuthUtils.addParameter(callbackUrl, "code", ticket);
        logger.debug("callbackUrlWithCode : {}", callbackUrlWithCode);
        return OAuthUtils.redirectTo(callbackUrlWithCode);
    }
}
