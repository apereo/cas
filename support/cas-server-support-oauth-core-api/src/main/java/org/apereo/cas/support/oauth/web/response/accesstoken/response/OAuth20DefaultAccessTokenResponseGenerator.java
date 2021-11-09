package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DefaultAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultAccessTokenResponseGenerator implements OAuth20AccessTokenResponseGenerator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * JWT builder.
     */
    protected final JwtBuilder accessTokenJwtBuilder;

    private final CasConfigurationProperties casProperties;

    private static boolean shouldGenerateDeviceFlowResponse(final OAuth20AccessTokenResponseResult result) {
        val generatedToken = result.getGeneratedToken();
        return OAuth20ResponseTypes.DEVICE_CODE == result.getResponseType()
            && generatedToken.getDeviceCode().isPresent()
            && generatedToken.getUserCode().isPresent()
            && generatedToken.getAccessToken().isEmpty();
    }

    @Audit(action = AuditableActions.OAUTH2_ACCESS_TOKEN_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_ACCESS_TOKEN_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_ACCESS_TOKEN_RESPONSE_RESOURCE_RESOLVER)
    @Override
    @SneakyThrows
    public ModelAndView generate(final WebContext webContext,
                                 final OAuth20AccessTokenResponseResult result) {
        if (shouldGenerateDeviceFlowResponse(result)) {
            return generateResponseForDeviceToken(webContext, result);
        }

        return generateResponseForAccessToken(webContext, result);
    }

    /**
     * Generate response for device token model and view.
     *
     * @param webContext the web context
     * @param result     the result
     * @return the model and view
     */
    @SneakyThrows
    protected ModelAndView generateResponseForDeviceToken(final WebContext webContext,
                                                          final OAuth20AccessTokenResponseResult result) {
        val model = getDeviceTokenResponseModel(result);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    /**
     * Gets device token response model.
     *
     * @param result the result
     * @return the device token response model
     */
    protected Map getDeviceTokenResponseModel(final OAuth20AccessTokenResponseResult result) {
        val model = new LinkedHashMap<String, Object>();
        val uri = result.getCasProperties().getServer().getPrefix()
            .concat(OAuth20Constants.BASE_OAUTH20_URL)
            .concat("/")
            .concat(OAuth20Constants.DEVICE_AUTHZ_URL);
        model.put(OAuth20Constants.DEVICE_VERIFICATION_URI, uri);
        model.put(OAuth20Constants.EXPIRES_IN, result.getDeviceTokenTimeout());
        val generatedToken = result.getGeneratedToken();
        generatedToken.getUserCode().ifPresent(c -> model.put(OAuth20Constants.DEVICE_USER_CODE, c));
        generatedToken.getDeviceCode().ifPresent(c -> model.put(OAuth20Constants.DEVICE_CODE, c));
        model.put(OAuth20Constants.DEVICE_INTERVAL, result.getDeviceRefreshInterval());
        return model;
    }

    /**
     * Generate response for access token model and view.
     *
     * @param webContext the web context
     * @param result     the result
     * @return the model and view
     */
    protected ModelAndView generateResponseForAccessToken(final WebContext webContext,
                                                          final OAuth20AccessTokenResponseResult result) {
        val model = getAccessTokenResponseModel(webContext, result);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    /**
     * Generate internal.
     *
     * @param webContext the web context
     * @param result     the result
     * @return the access token response model
     */
    protected Map<String, Object> getAccessTokenResponseModel(final WebContext webContext,
                                                              final OAuth20AccessTokenResponseResult result) {
        val model = new LinkedHashMap<String, Object>();
        val generatedToken = result.getGeneratedToken();
        generatedToken.getAccessToken().ifPresent(t -> {
            model.put(OAuth20Constants.ACCESS_TOKEN, encodeAccessToken(t, result));
            model.put(OAuth20Constants.SCOPE, String.join(" ", t.getScopes()));
            model.put(OAuth20Constants.EXPIRES_IN, t.getExpiresIn());
        });
        generatedToken.getRefreshToken().ifPresent(t -> model.put(OAuth20Constants.REFRESH_TOKEN, t.getId()));
        model.put(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
        return model;
    }

    /**
     * Encode access token string.
     *
     * @param accessToken the access token
     * @param result      the result
     * @return the string
     */
    protected String encodeAccessToken(final OAuth20AccessToken accessToken,
                                       final OAuth20AccessTokenResponseResult result) {
        return getAccessTokenBuilder(accessToken, result).build().encode();
    }

    /**
     * Gets access token builder.
     *
     * @param accessToken the access token
     * @param result      the result
     * @return the jwt access token builder
     */
    protected OAuth20JwtAccessTokenEncoder.OAuth20JwtAccessTokenEncoderBuilder getAccessTokenBuilder(
        final OAuth20AccessToken accessToken, final OAuth20AccessTokenResponseResult result) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(result.getRegisteredService())
            .service(result.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties);
    }
}
