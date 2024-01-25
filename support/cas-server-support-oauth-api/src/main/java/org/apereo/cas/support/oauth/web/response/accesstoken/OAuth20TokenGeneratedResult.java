package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
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
public class OAuth20TokenGeneratedResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -4111380934505564576L;

    private final Ticket accessToken;

    private final Ticket refreshToken;

    private final OAuth20ResponseTypes responseType;

    private final OAuth20GrantTypes grantType;

    private final RegisteredService registeredService;

    private final String deviceCode;

    private final String userCode;

    @Builder.Default
    @Getter
    private final Map<String, Object> details = new LinkedHashMap<>();

    public Optional<Ticket> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<Ticket> getRefreshToken() {
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
}
