package org.jasig.cas.support.oauth.provider.impl;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jasig.cas.support.oauth.provider.BaseOAuth20Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GitHubApi;

/**
 * This is the provider for GitHub.
 * 
 * @author Jerome Leleu
 */
public class GitHubProvider extends BaseOAuth20Provider {
    
    @Override
    protected void initService() {
        service = new ServiceBuilder().provider(GitHubApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl)
            .scope("user").build();
    }
    
    @Override
    protected String getProfileUrl() {
        return "https://github.com/api/v2/json/user/show";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = null;
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONObject jsonUser = (JSONObject) jsonObject.get("user");
            userId = jsonUser.getString("id");
            logger.debug("userId : {}", userId);
        } catch (JSONException e) {
            logger.error("JSON exception", e);
            return null;
        }
        return userId;
    }
}
