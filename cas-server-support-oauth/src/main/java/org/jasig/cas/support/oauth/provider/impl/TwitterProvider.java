package org.jasig.cas.support.oauth.provider.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.support.oauth.provider.BaseOAuth10Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;

/**
 * This class is the Twitter provider to authenticate user in Twitter.
 * 
 * @author Jerome Leleu
 */
public class TwitterProvider extends BaseOAuth10Provider {
    
    @Override
    public void initService() {
        service = new ServiceBuilder().provider(TwitterApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl)
            .build();
    }
    
    @Override
    protected String getProfileUrl() {
        return "http://api.twitter.com/1/account/verify_credentials.xml";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = StringUtils.substringAfter(StringUtils.substringBefore(body, "</id>"), "<id>");
        logger.debug("userId : {}", userId);
        return userId;
    }
}
