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
package org.jasig.cas.support.oauth.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.support.oauth.provider.OAuthProviders;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.provider.BaseOAuth10Provider;
import org.scribe.up.provider.BaseOAuthProvider;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.session.HttpUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class represents an action in the webflow to retrieve OAuth information on the callback url which is the webflow url (/login). The
 * oauth_provider and the other OAuth parameters are expected after OAuth authentication. Providers are defined by configuration. The
 * service is stored and retrieved from web session after OAuth authentication.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthAction extends AbstractAction {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthAction.class);
    
    @NotNull
    private OAuthProviders providers;
    
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;
    
    private String oauth10loginUrl = "/" + OAuthConstants.OAUTH10_LOGIN_URL;
    
    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        HttpSession session = request.getSession();
        
        // get provider type
        String providerType = request.getParameter(OAuthConstants.OAUTH_PROVIDER);
        logger.debug("providerType : {}", providerType);
        
        // it's an authentication
        if (StringUtils.isNotBlank(providerType)) {
            // get provider
            OAuthProvider provider = OAuthUtils.getProviderByType(providers, providerType);
            logger.debug("provider : {}", provider);
            
            // get credential
            @SuppressWarnings("unchecked")
            OAuthCredential credential = provider
                .getCredential(new HttpUserSession(request), request.getParameterMap());
            logger.debug("credential : {}", credential);
            
            // retrieve service from session and put it into webflow
            Service service = (Service) session.getAttribute("service");
            context.getFlowScope().put("service", service);
            
            // create credentials
            Credentials credentials = new OAuthCredentials(credential);
            
            try {
                WebUtils.putTicketGrantingTicketInRequestScope(context, this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials));
                return success();
            } catch (final TicketException e) {
                return error();
            }
        } else {
            // no authentication : go to login page
            
            // put service in session from flow scope
            Service service = (Service) context.getFlowScope().get("service");
            session.setAttribute("service", service);
            
            // for all providers, generate authorization urls
            for (OAuthProvider provider : providers.getProviders()) {
                String key = provider.getType() + "Url";
                String authorizationUrl = null;
                // for OAuth 1.0 protocol, delay request_token request by pointing to an intermediate url
                if (provider instanceof BaseOAuth10Provider) {
                    authorizationUrl = OAuthUtils.addParameter(request.getContextPath() + oauth10loginUrl,
                                                               OAuthConstants.OAUTH_PROVIDER, provider.getType());
                } else {
                    authorizationUrl = provider.getAuthorizationUrl(new HttpUserSession(session));
                }
                logger.debug("{} -> {}", key, authorizationUrl);
                request.setAttribute(key, authorizationUrl);
            }
        }
        
        return error();
    }
    
    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
    
    public void setOauth10loginUrl(final String oauth10loginUrl) {
        this.oauth10loginUrl = oauth10loginUrl;
    }
    
    public void setProviders(final OAuthProviders providers) {
        this.providers = providers;
        // for all providers
        for (OAuthProvider provider : providers.getProviders()) {
            BaseOAuthProvider baseProvider = (BaseOAuthProvider) provider;
            // calculate new callback url by adding the OAuth provider type
            baseProvider.setCallbackUrl(OAuthUtils.addParameter(baseProvider.getCallbackUrl(),
                                                                OAuthConstants.OAUTH_PROVIDER, provider.getType()));
        }
    }
}
