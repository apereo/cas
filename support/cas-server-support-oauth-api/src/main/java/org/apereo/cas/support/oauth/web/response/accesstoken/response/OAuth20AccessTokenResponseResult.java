package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import module java.base;
import org.apereo.cas.audit.AuditableEntity;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.val;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20AccessTokenResponseResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SuperBuilder
@Getter
@Jacksonized
public class OAuth20AccessTokenResponseResult implements Serializable, AuditableEntity {

    @Serial
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

    private final OAuth20TokenExchangeTypes requestedTokenType;

    private final Service tokenExchangeResource;

    private final String tokenExchangeAudience;

    private final String cibaRequestId;
    
    @Override
    @JsonIgnore
    public String getAuditablePrincipal() {
        if (userProfile != null) {
            val principal = (Principal) userProfile.getAttribute(Principal.class.getName());
            return principal != null ? principal.getId() : userProfile.getId();
        }
        return AuditableEntity.super.getAuditablePrincipal();
    }
}
