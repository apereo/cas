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
package org.jasig.cas.support.oauth.authentication.handler.support;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.scribe.model.Token;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.session.HttpUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.context.ExternalContextHolder;

/**
 * This handler authenticates OAuth credentials : it uses them to get an access token to get the user profile returned by the provider for
 * an authenticated user.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticationHandler.class);
    
    @NotNull
    private List<OAuthProvider> providers;
    
    public boolean supports(Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
    
    @Override
    protected boolean doAuthentication(Credentials credentials) throws AuthenticationException {
        OAuthCredentials credential = (OAuthCredentials) credentials;
        logger.debug("credential : {}", credential);
        
        String providerType = credential.getProviderType();
        logger.debug("providerType : {}", providerType);
        // get provider
        OAuthProvider provider = null;
        for (OAuthProvider aProvider : providers) {
            if (StringUtils.equals(providerType, aProvider.getType())) {
                provider = aProvider;
                break;
            }
        }
        logger.debug("provider : {}", provider);
        
        // get access token
        HttpServletRequest request = (HttpServletRequest) ExternalContextHolder.getExternalContext().getNativeRequest();
        Token accessToken = provider.getAccessToken(new HttpUserSession(request.getSession()), credential);
        logger.debug("accessToken : {}", accessToken);
        // and user profile
        UserProfile userProfile = provider.getUserProfile(accessToken);
        logger.debug("userProfile : {}", userProfile);
        
        if (userProfile != null && StringUtils.isNotBlank(userProfile.getId())) {
            userProfile.addAttribute("access_token", accessToken.getToken());
            credential.setUserId(userProfile.getId());
            credential.setUserAttributes(userProfile.getAttributes());
            return true;
        } else {
            return false;
        }
    }
    
    public void setProviders(List<OAuthProvider> providers) {
        this.providers = providers;
    }
}
