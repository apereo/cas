package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;

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
    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private OAuth20ResponseTypes responseType;
    private OAuth20GrantTypes grantType;
    private OAuthRegisteredService registeredService;
    private String deviceCode;
    private String userCode;

    @Builder.Default
    private Map<String, Object> details = new LinkedHashMap<>();

    public Optional<AccessToken> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<RefreshToken> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public Optional<OAuth20ResponseTypes> getResponseType() {
        return Optional.ofNullable(responseType);
    }

    public Optional<OAuth20GrantTypes> getGrantType() {
        return Optional.ofNullable(grantType);
    }

    public Optional<OAuthRegisteredService> getRegisteredService() {
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
