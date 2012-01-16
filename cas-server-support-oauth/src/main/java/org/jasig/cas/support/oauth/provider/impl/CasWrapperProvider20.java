package org.jasig.cas.support.oauth.provider.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.support.oauth.provider.BaseOAuth20Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.CasWrapperApi20;

/**
 * This class is the identity provider to authenticate user in CAS wrapping OAuth protocol.
 * 
 * @author Jerome Leleu
 */
public class CasWrapperProvider20 extends BaseOAuth20Provider {
    
    private String serverUrl;
    
    @Override
    protected void initService() {
        CasWrapperApi20.setServerUrl(serverUrl);
        service = new ServiceBuilder().provider(CasWrapperApi20.class).apiKey(key).apiSecret(secret)
            .callback(callbackUrl).build();
    }
    
    @Override
    protected String getProfileUrl() {
        return serverUrl + "/profile";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = StringUtils.substringAfter(StringUtils.substringBefore(body, "</id>"), "<id>");
        logger.debug("userId : {}", userId);
        return userId;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
