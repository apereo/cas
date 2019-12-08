package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.Builder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OAuth20TokenGeneratedResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Builder
public class OAuth20TokenGeneratedResult {
    private static final int MAP_SIZE = 8;

    private OAuth20AccessToken accessToken;
    private OAuth20RefreshToken refreshToken;
    private OAuth20ResponseTypes responseType;
    private OAuth20GrantTypes grantType;
    private RegisteredService registeredService;
    private String deviceCode;
    private String userCode;

    @Builder.Default
    private Map<String, Object> details = new LinkedHashMap<>(MAP_SIZE);

    public Optional<OAuth20AccessToken> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<OAuth20RefreshToken> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public Optional<OAuth20ResponseTypes> getResponseType() {
        return Optional.ofNullable(responseType);
    }

    public Optional<OAuth20GrantTypes> getGrantType() {
        return Optional.ofNullable(grantType);
    }

    public Optional<RegisteredService> getRegisteredService() {
        return Optional.ofNullable(registeredService);
    }

    public Optional<String> getDeviceCode() {
        return Optional.ofNullable(deviceCode);
    }

    public Optional<String> getUserCode() {
        return Optional.ofNullable(userCode);
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
