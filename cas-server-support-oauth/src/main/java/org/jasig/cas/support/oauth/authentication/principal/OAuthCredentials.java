/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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
