package org.jasig.cas.support.oauth.provider.impl;

import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.BaseOAuth20Provider;

/**
 * This class is the OAuth provider to authenticate user in CAS wrapping OAuth protocol.
 * 
 * @author Jerome Leleu
 */
public class CasWrapperProvider20 extends BaseOAuth20Provider {
    
    private String serverUrl;
    
    @Override
    protected void internalInit() {
        CasWrapperApi20.setServerUrl(serverUrl);
        service = new ServiceBuilder().provider(CasWrapperApi20.class).apiKey(key).apiSecret(secret)
            .callback(callbackUrl).build();
    }
    
    @Override
    protected String getProfileUrl() {
        return serverUrl + "/profile";
    }
    
    @Override
    protected UserProfile extractUserProfile(String body) {
        UserProfile userProfile = new UserProfile();
        JsonNode json = profileHelper.getFirstJsonNode(body);
        if (json != null) {
            profileHelper.addIdentifier(userProfile, json, "id");
            json = json.get("attributes");
            if (json != null) {
                Iterator<JsonNode> nodes = json.iterator();
                while (nodes.hasNext()) {
                    json = nodes.next();
                    profileHelper.addAttribute(userProfile, json, json.getFieldNames().next());
                }
            }
        }
        return userProfile;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
