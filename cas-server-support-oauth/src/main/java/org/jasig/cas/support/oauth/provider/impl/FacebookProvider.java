package org.jasig.cas.support.oauth.provider.impl;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jasig.cas.support.oauth.provider.BaseOAuth20Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;

/**
 * This class is the Facebook provider to authenticate user in Facebook.
 * 
 * @author Jerome Leleu
 */
public class FacebookProvider extends BaseOAuth20Provider {
    
    @Override
    public void initService() {
        service = new ServiceBuilder().provider(FacebookApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl)
            .build();
    }
    
    @Override
    protected String getProfileUrl() {
        return "https://graph.facebook.com/me";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = null;
        try {
            JSONObject jsonObject = new JSONObject(body);
            userId = (String) jsonObject.get("id");
            logger.debug("userId : {}", userId);
        } catch (JSONException e) {
            logger.error("JSON exception", e);
            return null;
        }
        return userId;
    }
}
