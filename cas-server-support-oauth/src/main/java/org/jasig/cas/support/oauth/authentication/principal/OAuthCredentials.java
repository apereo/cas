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
package org.jasig.cas.support.oauth.authentication.principal;

import java.util.Map;

import org.jasig.cas.authentication.principal.Credentials;
import org.scribe.up.credential.OAuthCredential;

/**
 * This class represents an OAuth credential and (after authentication) user identifier and attributes.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthCredentials extends OAuthCredential implements Credentials {
    
    private static final long serialVersionUID = 2759505049384101768L;
    
    private String userId;
    
    private Map<String, Object> userAttributes;
    
    public OAuthCredentials(OAuthCredential credential) {
        super(credential.getToken(), credential.getVerifier(), credential.getProviderType());
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Map<String, Object> getUserAttributes() {
        return userAttributes;
    }
    
    public void setUserAttributes(Map<String, Object> userAttributes) {
        this.userAttributes = userAttributes;
    }
}
