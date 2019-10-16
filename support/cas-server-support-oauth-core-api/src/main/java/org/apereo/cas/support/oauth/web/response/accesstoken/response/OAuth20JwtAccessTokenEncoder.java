package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
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
    private final OAuth20AccessToken accessToken;
    private final RegisteredService registeredService;
    private final Service service;

    /**
     * Encode access token as JWT.
     *
     * @return the string
     */
    public String encode() {
        val oAuthRegisteredService = OAuthRegisteredService.class.cast(this.registeredService);
        val authentication = accessToken.getAuthentication();
        if (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken()) {
            val request = getJwtRequestBuilder(oAuthRegisteredService, authentication);
            return accessTokenJwtBuilder.build(request);
        }

        return accessToken.getId();
    }

    /**
     * Decode access token as JWT..
     *
     * @param tokenId the token id
     * @return the string
     */
    @SneakyThrows
    public String decode(final String tokenId) {
        val oAuthRegisteredService = OAuthRegisteredService.class.cast(this.registeredService);
        val authentication = accessToken.getAuthentication();
        if (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken()) {
            val request = getJwtRequestBuilder(oAuthRegisteredService, authentication);
            val claims = accessTokenJwtBuilder.unpack(request, tokenId);
            return claims.getJWTID();
        }
        return tokenId;
    }

    /**
     * Gets jwt request builder.
     *
     * @param oAuthRegisteredService the o auth registered service
     * @param authentication         the authentication
     * @return the jwt request builder
     */
    protected JwtBuilder.JwtRequest getJwtRequestBuilder(final OAuthRegisteredService oAuthRegisteredService,
                                                         final Authentication authentication) {
        val builder = JwtBuilder.JwtRequest.builder();
        val dt = authentication.getAuthenticationDate().plusSeconds(accessToken.getExpirationPolicy().getTimeToLive());
        return builder
            .serviceAudience(service.getId())
            .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
            .jwtId(accessToken.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(DateTimeUtils.dateOf(dt))
            .attributes(authentication.getAttributes())
            .registeredService(oAuthRegisteredService)
            .build();
    }
}
