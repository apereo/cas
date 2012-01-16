package org.jasig.cas.support.oauth.provider.impl;

import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jasig.cas.support.oauth.provider.BaseOAuth10Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.Token;

/**
 * This class is the Google provider to authenticate user in Google.
 * 
 * @author Jerome Leleu
 */
public class GoogleProvider extends BaseOAuth10Provider {
    
    @Override
    public void initService() {
        service = new ServiceBuilder().provider(GoogleApi.class).apiKey(key).apiSecret(secret)
            .scope("http://www-opensocial.googleusercontent.com/api/people/").callback(callbackUrl).build();
    }
    
    @Override
    public String getAuthorizationUrl(HttpSession session) {
        Token requestToken = service.getRequestToken();
        logger.debug("requestToken : {}", requestToken);
        // save requestToken in session
        session.setAttribute(name + "#tokenRequest", requestToken);
        String authorizationUrl = "https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token="
                                  + requestToken.getToken();
        logger.debug("authorizationUrl : {}", authorizationUrl);
        return authorizationUrl;
    }
    
    @Override
    protected String getProfileUrl() {
        return "http://www-opensocial.googleusercontent.com/api/people/@me/@self";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = null;
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONObject jsonEntry = (JSONObject) jsonObject.get("entry");
            userId = jsonEntry.getString("id");
            logger.debug("userId : {}", userId);
        } catch (JSONException e) {
            logger.error("JSON exception", e);
            return null;
        }
        return userId;
    }
}
