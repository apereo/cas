package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.Builder;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OAuth20TokenGeneratedResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SuperBuilder
@ToString(doNotUseGetters = true, exclude = "registeredService")
public class OAuth20TokenGeneratedResult {
    private final OAuth20AccessToken accessToken;

    private final OAuth20RefreshToken refreshToken;

    private final OAuth20ResponseTypes responseType;

    private final OAuth20GrantTypes grantType;

    private final RegisteredService registeredService;

    private final String deviceCode;

    private final String userCode;

    @Builder.Default
    private final Map<String, Object> details = new LinkedHashMap<>();

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
