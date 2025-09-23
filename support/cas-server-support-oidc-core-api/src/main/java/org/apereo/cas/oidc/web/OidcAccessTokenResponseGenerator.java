package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20DefaultAccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.OidcIdToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcAccessTokenResponseGenerator extends OAuth20DefaultAccessTokenResponseGenerator<OidcConfigurationContext> {

    public OidcAccessTokenResponseGenerator(final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext) {
        super(oidcConfigurationContext);
    }

    @Override
    protected String encodeOAuthToken(final OAuth20Token accessToken,
                                      final OAuth20AccessTokenResponseResult result) {
        val oidcRegisteredService = Optional.ofNullable(result.getRegisteredService())
            .filter(OidcRegisteredService.class::isInstance)
            .map(OidcRegisteredService.class::cast);
        val oidcIssuer = configurationContext.getObject().getIssuerService().determineIssuer(oidcRegisteredService);
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext.getObject(), result, accessToken, oidcIssuer);
        return cipher.encode(accessToken.getId(), new Object[]{accessToken, result});
    }

    @Override
    protected Map<String, Object> getAccessTokenResponseModel(final OAuth20AccessTokenResponseResult result) {
        val accessToken = result.getGeneratedToken().getAccessToken();
        val model = super.getAccessTokenResponseModel(result);

        if (result.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE) {
            buildResponseModelForTokenExchange(result, model);
        } else {
            accessToken.map(at -> resolveToken(at, OAuth20AccessToken.class))
                .ifPresent(token -> {
                    if (result.getRegisteredService() instanceof OidcRegisteredService
                        && !token.getScopes().contains(OidcConstants.CLIENT_REGISTRATION_SCOPE)) {
                        collectIdToken(result, token, model);
                    }
                });
        }
        return model;
    }

    protected void buildResponseModelForTokenExchange(final OAuth20AccessTokenResponseResult result, final Map<String, Object> model) {
        if (result.getRequestedTokenType() == OAuth20TokenExchangeTypes.ID_TOKEN) {
            val accessToken = result.getGeneratedToken().getAccessToken();
            accessToken.map(at -> resolveToken(at, OAuth20AccessToken.class)).ifPresent(at -> collectIdToken(result, at, model));
        }
        model.put(OAuth20Constants.ISSUED_TOKEN_TYPE, result.getRequestedTokenType().getType());
    }

    protected void collectIdToken(final OAuth20AccessTokenResponseResult result,
                                  final OAuth20AccessToken token,
                                  final Map<String, Object> model) {
        val idToken = generateIdToken(result, token);
        if (idToken != null) {
            val idTokenValue = idToken.token();
            LOGGER.debug("Generated ID token [{}] based on grant type [{}]", idTokenValue, result.getGrantType());
            FunctionUtils.doIfNotBlank(idTokenValue, v -> model.put(OidcConstants.ID_TOKEN, v));
            FunctionUtils.doIfNotBlank(idToken.deviceSecret(), v -> model.put(OidcConstants.DEVICE_SECRET, v));
        }
    }

    protected OidcIdToken generateIdToken(final OAuth20AccessTokenResponseResult result,
                                          final OAuth20AccessToken accessToken) {
        return FunctionUtils.doUnchecked(() -> {
            val refreshToken = result.getGeneratedToken().getRefreshToken().orElse(null);
            var idTokenContext = IdTokenGenerationContext
                .builder()
                .accessToken(accessToken)
                .userProfile(result.getUserProfile())
                .responseType(result.getResponseType())
                .grantType(result.getGrantType())
                .registeredService((OAuthRegisteredService) result.getRegisteredService())
                .refreshToken(resolveToken(refreshToken, OAuth20RefreshToken.class))
                .build();
            LOGGER.debug("Generating ID token for access token [{}]", accessToken.getId());
            return configurationContext.getObject().getIdTokenGeneratorService().generate(idTokenContext);
        });
    }
}

