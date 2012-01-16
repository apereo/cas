package org.jasig.cas.support.oauth.provider.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.support.oauth.provider.BaseOAuth10Provider;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;

/**
 * This class is the Yahoo provider to authenticate user in Yahoo.
 * 
 * @author Jerome Leleu
 */
public class YahooProvider extends BaseOAuth10Provider {
    
    @Override
    protected void initService() {
        service = new ServiceBuilder().provider(YahooApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl)
            .build();
    }
    
    @Override
    protected String getProfileUrl() {
        return "http://social.yahooapis.com/v1/me/guid?format=xml";
    }
    
    @Override
    protected String extractUserId(String body) {
        String userId = StringUtils.substringAfter(StringUtils.substringBefore(body, "</value>"), "<value>");
        logger.debug("userId : {}", userId);
        return userId;
    }
}
