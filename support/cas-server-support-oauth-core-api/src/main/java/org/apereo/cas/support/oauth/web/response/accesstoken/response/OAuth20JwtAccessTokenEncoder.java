package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import com.nimbusds.jwt.JWTParser;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Optional;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Builder
@Getter
@Slf4j
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
        if (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken()) {
            val request = getJwtRequestBuilder(oAuthRegisteredService, accessToken);
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
        try {
            if (StringUtils.isBlank(tokenId)) {
                LOGGER.warn("No access token is provided to decode");
                return null;
            }
            val header = JWTParser.parse(tokenId).getHeader();
            var oAuthRegisteredService = (OAuthRegisteredService) this.registeredService;
            if (oAuthRegisteredService == null) {
                val serviceId = header.getCustomParam(RegisteredServiceCipherExecutor.CUSTOM_HEADER_REGISTERED_SERVICE_ID);
                if (serviceId != null) {
                    val serviceIdentifier = Long.parseLong(serviceId.toString());
                    oAuthRegisteredService = accessTokenJwtBuilder.getServicesManager()
                        .findServiceBy(serviceIdentifier, OAuthRegisteredService.class);
                }
            }
            val claims = accessTokenJwtBuilder.unpack(Optional.ofNullable(oAuthRegisteredService), tokenId);
            return claims.getJWTID();
        } catch (final ParseException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return tokenId;
    }

    /**
     * Gets jwt request builder.
     *
     * @param oAuthRegisteredService the o auth registered service
     * @param accessToken            the access token
     * @return the jwt request builder
     */
    protected JwtBuilder.JwtRequest getJwtRequestBuilder(final OAuthRegisteredService oAuthRegisteredService,
                                                         final OAuth20AccessToken accessToken) {
        val authentication = accessToken.getAuthentication();
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
