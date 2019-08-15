package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
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

    private final ResourceLoader resourceLoader;
    private final RegisteredService registeredService;
    private final Service service;
    private final OAuth20TokenGeneratedResult generatedToken;
    private final long accessTokenTimeout;
    private final long deviceTokenTimeout;
    private final OAuth20ResponseTypes responseType;
    private final CasConfigurationProperties casProperties;
    private final long deviceRefreshInterval;
}
