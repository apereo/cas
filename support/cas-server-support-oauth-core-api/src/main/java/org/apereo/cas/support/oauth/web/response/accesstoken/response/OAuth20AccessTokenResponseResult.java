package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.ResourceLoader;

/**
 * This is {@link OAuth20AccessTokenResponseResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Builder
@Getter
public class OAuth20AccessTokenResponseResult {

    private ResourceLoader resourceLoader;
    private OAuthRegisteredService registeredService;
    private Service service;
    private OAuth20TokenGeneratedResult generatedToken;
    private long accessTokenTimeout;
    private long deviceTokenTimeout;
    private OAuth20ResponseTypes responseType;
    private CasConfigurationProperties casProperties;
    private long deviceRefreshInterval;
}
