package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * This class represents an OAuth credentials : a provider name, a provider type, a token, a verifier and (after authentication) a user
 * identifier.
 * 
 * @author Jerome Leleu
 */
public class OAuthCredentials implements Credentials {
    
    private static final long serialVersionUID = 6087571792762123419L;
    
    private String providerName;
    
    private String providerType;
    
    private String token;
    
    private String verifier;
    
    private String userId = null;
    
    public OAuthCredentials(String providerName, String providerType, String token, String verifier) {
        this.providerName = providerName;
        this.providerType = providerType;
        this.token = token;
        this.verifier = verifier;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public String getProviderType() {
        return providerType;
    }
    
    public String getToken() {
        return token;
    }
    
    public String getVerifier() {
        return verifier;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
