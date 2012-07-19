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

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.support.oauth.provider.OAuthProviders;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.OAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler authenticates OAuth credentials : it uses them to get the user profile returned by the provider for an authenticated user.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticationHandler.class);
    
    private OAuthProviders providers;
    
    public boolean supports(final Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
    
    @Override
    protected boolean doAuthentication(final Credentials credentials) throws AuthenticationException {
        OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        logger.debug("credential : {}", oauthCredentials);
        
        String providerType = oauthCredentials.getCredential().getProviderType();
        logger.debug("providerType : {}", providerType);
        
        // get provider
        OAuthProvider provider = OAuthUtils.getProviderByType(providers, providerType);
        logger.debug("provider : {}", provider);
        
        // get user profile
        UserProfile userProfile = provider.getUserProfile(oauthCredentials.getCredential());
        logger.debug("userProfile : {}", userProfile);
        
        if (userProfile != null && StringUtils.isNotBlank(userProfile.getId())) {
            oauthCredentials.setUserProfile(userProfile);
            return true;
        } else {
            return false;
        }
    }
    
    public void setProviders(final OAuthProviders providers) {
        this.providers = providers;
    }
}
