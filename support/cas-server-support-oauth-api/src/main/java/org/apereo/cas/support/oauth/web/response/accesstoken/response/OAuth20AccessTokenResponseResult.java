package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.pac4j.core.profile.UserProfile;

import java.io.Serializable;

/**
 * This is {@link OAuth20AccessTokenResponseResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SuperBuilder
@Getter
@Jacksonized
public class OAuth20AccessTokenResponseResult implements Serializable {

    private static final long serialVersionUID = -1229778562782271609L;

    private final RegisteredService registeredService;

    private final Service service;

    private final OAuth20TokenGeneratedResult generatedToken;

    private final long accessTokenTimeout;

    private final long deviceTokenTimeout;

    private final OAuth20ResponseTypes responseType;

    private final OAuth20GrantTypes grantType;

    private final CasConfigurationProperties casProperties;

    private final long deviceRefreshInterval;

    private final UserProfile userProfile;
}
