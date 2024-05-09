package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20DefaultAccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcAccessTokenResponseGenerator extends OAuth20DefaultAccessTokenResponseGenerator {
    protected final IdTokenGeneratorService idTokenGenerator;

    private final OidcIssuerService oidcIssuerService;

    public OidcAccessTokenResponseGenerator(final IdTokenGeneratorService idTokenGenerator,
                                            final JwtBuilder jwtBuilder,
                                            final TicketRegistry ticketRegistry,
                                            final CasConfigurationProperties casProperties,
                                            final OidcIssuerService oidcIssuerService) {
        super(jwtBuilder, ticketRegistry, casProperties);
        this.idTokenGenerator = idTokenGenerator;
        this.oidcIssuerService = oidcIssuerService;
    }

    @Override
    protected String encodeAccessToken(final OAuth20AccessToken accessToken,
                                       final OAuth20AccessTokenResponseResult result) {
        val oidcRegisteredService = Optional.ofNullable(result.getRegisteredService())
            .filter(OidcRegisteredService.class::isInstance)
            .map(OidcRegisteredService.class::cast);
        val oidcIssuer = oidcIssuerService.determineIssuer(oidcRegisteredService);
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(accessTokenJwtBuilder, result,
            accessToken, oidcIssuer, casProperties);
        return cipher.encode(accessToken.getId(), new Object[]{accessToken, result});
    }

    @Override
    protected Map<String, Object> getAccessTokenResponseModel(final OAuth20AccessTokenResponseResult result) {
        val accessToken = result.getGeneratedToken().getAccessToken();

        if (result.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE) {
            if (result.getRequestedTokenType() == OAuth20TokenExchangeTypes.ID_TOKEN) {
                return accessToken
                    .map(OAuth20AccessToken.class::cast)
                    .map(token -> {
                        val idToken = generateIdToken(result, token);
                        val model = new HashMap<String, Object>();
                        FunctionUtils.doIfNotBlank(idToken, __ -> model.put(OidcConstants.ID_TOKEN, idToken));
                        model.put(OAuth20Constants.ISSUED_TOKEN_TYPE, result.getRequestedTokenType().getType());
                        return model;
                    })
                    .orElseThrow();
            }
            return super.getAccessTokenResponseModel(result);
        }

        val model = super.getAccessTokenResponseModel(result);
        accessToken.map(at -> resolveToken(at, OAuth20AccessToken.class)).ifPresent(token -> {
            if (result.getRegisteredService() instanceof OidcRegisteredService && !token.getScopes().contains(OidcConstants.CLIENT_REGISTRATION_SCOPE)) {
                val idToken = generateIdToken(result, token);
                FunctionUtils.doIfNotBlank(idToken, __ -> model.put(OidcConstants.ID_TOKEN, idToken));
            }
        });
        return model;
    }

    protected String generateIdToken(final OAuth20AccessTokenResponseResult result,
                                     final OAuth20AccessToken accessToken) {
        return FunctionUtils.doUnchecked(() -> {

            val refreshToken = result.getGeneratedToken().getRefreshToken().orElse(null);
            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(result.getUserProfile())
                .responseType(result.getResponseType())
                .grantType(result.getGrantType())
                .registeredService((OAuthRegisteredService) result.getRegisteredService())
                .refreshToken(resolveToken(refreshToken, OAuth20RefreshToken.class))
                .build();
            val idTokenGenerated = idTokenGenerator.generate(idTokenContext);
            if (idTokenGenerated != null) {
                val idToken = idTokenGenerated.token();
                LOGGER.debug("Generated ID token [{}]", idToken);
                return idToken;
            }
            return null;
        });
    }
}

