package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.val;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Builder
@Getter
public class OAuth20JwtAccessTokenEncoder {
    private final JwtBuilder accessTokenJwtBuilder;
    private final AccessToken accessToken;
    private final RegisteredService registeredService;
    private final Service service;

    public String encode() {
        val oAuthRegisteredService = OAuthRegisteredService.class.cast(this.registeredService);
        val authentication = accessToken.getAuthentication();
        if (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken()) {
            val dt = authentication.getAuthenticationDate().plusSeconds(accessToken.getExpirationPolicy().getTimeToLive());
            val builder = JwtBuilder.JwtRequest.builder();

            val request = builder
                .serviceAudience(service.getId())
                .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                .jwtId(accessToken.getId())
                .subject(authentication.getPrincipal().getId())
                .validUntilDate(DateTimeUtils.dateOf(dt))
                .attributes(authentication.getAttributes())
                .build();
            return accessTokenJwtBuilder.build(request);
        }

        return accessToken.getId();
    }
}
