package org.jasig.cas.support.oauth.authentication.principal;

import java.util.Map;

import org.jasig.cas.authentication.principal.Credentials;
import org.scribe.up.credential.OAuthCredential;

/**
 * This class represents an OAuth credential and (after authentication) a user identifier and attributes.
 * 
 * @author Jerome Leleu
 */
public class OAuthCredentials extends OAuthCredential implements Credentials {
    
    private static final long serialVersionUID = -6751617306486628735L;
    
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
