package org.jasig.cas.support.oauth.provider.impl;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.utils.OAuthEncoder;

/**
 * This class represents the OAuth API implementation for the CAS OAuth wrapper.
 * 
 * @author Jerome Leleu
 */
public class CasWrapperApi20 extends DefaultApi20 {
    
    private static String serverUrl = "";
    
    @Override
    public String getAccessTokenEndpoint() {
        return serverUrl + "/accessToken?";
    }
    
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(serverUrl + "/authorize?client_id=%s&redirect_uri=%s", config.getApiKey(),
                             OAuthEncoder.encode(config.getCallback()));
    }
    
    public static void setServerUrl(String url) {
        serverUrl = url;
    }
}
