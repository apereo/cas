package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.dpop.JWKThumbprintConfirmation;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OAuth20JwtAccessTokenEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SuperBuilder
@Getter
@Slf4j
public class OAuth20JwtAccessTokenEncoder implements CipherExecutor<String, String> {
    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20AccessToken accessToken;

    private final RegisteredService registeredService;

    private final Service service;

    private final CasConfigurationProperties casProperties;

    private final String issuer;

    @Override
    public String decode(final String tokenId, final Object[] parameters) {
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

    @Override
    public String encode(final String value, final Object[] parameters) {
        val oAuthRegisteredService = (OAuthRegisteredService) this.registeredService;
        if (shouldEncodeAsJwt(oAuthRegisteredService, accessToken)) {
            val request = getJwtRequestBuilder(Optional.ofNullable(oAuthRegisteredService), accessToken);
            return accessTokenJwtBuilder.build(request);
        }
        return accessToken.getId();
    }

    protected JwtBuilder.JwtRequest getJwtRequestBuilder(
        final Optional<RegisteredService> registeredService,
        final OAuth20AccessToken accessToken) {
        val authentication = accessToken.getAuthentication();
        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        if (accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)) {
            CollectionUtils.firstElement(accessToken.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_CONFIRMATION))
                .ifPresent(conf -> {
                    val confirmation = new JWKThumbprintConfirmation(new Base64URL(conf.toString()));
                    val claim = confirmation.toJWTClaim();
                    attributes.put(claim.getKey(), List.of(claim.getValue()));
                });
        }

        val builder = JwtBuilder.JwtRequest.builder();
        val dt = authentication.getAuthenticationDate().plusSeconds(accessToken.getExpirationPolicy().getTimeToLive());
        return builder
            .serviceAudience(service.getId())
            .issueDate(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
            .jwtId(accessToken.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(DateTimeUtils.dateOf(dt))
            .attributes(attributes)
            .registeredService(registeredService)
            .issuer(StringUtils.defaultIfBlank(this.issuer, casProperties.getServer().getPrefix()))
            .build();
    }

    protected boolean shouldEncodeAsJwt(final OAuthRegisteredService oAuthRegisteredService,
                                        final OAuth20AccessToken accessToken) {
        return casProperties.getAuthn().getOauth().getAccessToken().isCreateAsJwt()
               || (oAuthRegisteredService != null && oAuthRegisteredService.isJwtAccessToken())
               || accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP);
    }
}
