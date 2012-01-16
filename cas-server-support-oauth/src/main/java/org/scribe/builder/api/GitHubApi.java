package org.scribe.builder.api;

import static org.scribe.utils.URLUtils.formURLEncode;

import org.scribe.model.OAuthConfig;
import org.scribe.utils.Preconditions;

/**
 * This class represents the OAuth API implementation for GitHub. Should be implemented natively in Scribe in further release.
 * 
 * @author Jerome Leleu
 */
public class GitHubApi extends DefaultApi20 {
    
    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";
    
    @Override
    public String getAccessTokenEndpoint() {
        return "https://github.com/login/oauth/access_token";
    }
    
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(config.getCallback(),
                                    "Must provide a valid url as callback. GitHub does not support OOB");
        
        // Append scope if present
        if (config.hasScope()) {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), formURLEncode(config.getCallback()),
                                 formURLEncode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL, config.getApiKey(), formURLEncode(config.getCallback()));
        }
    }
}
