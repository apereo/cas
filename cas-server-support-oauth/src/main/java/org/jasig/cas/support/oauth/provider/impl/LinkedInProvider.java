package org.jasig.cas.support.oauth.provider.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.support.oauth.provider.BaseOAuth10Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;

/**
 * This class is the LinkedIn provider to authenticate user in LinkedIn.
 * 
 * @author Jerome Leleu
 */
public class LinkedInProvider extends BaseOAuth10Provider {
    
    @Override
    protected void initService() {
        service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl)
            .build();
    }
    
    @Override
    protected String getProfileUrl() {
        return "http://api.linkedin.com/v1/people/~";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = StringUtils.substringAfter(StringUtils.substringBefore(body, "&amp;authToken="), "&amp;key=");
        logger.debug("userId : {}", userId);
        return userId;
    }
}
