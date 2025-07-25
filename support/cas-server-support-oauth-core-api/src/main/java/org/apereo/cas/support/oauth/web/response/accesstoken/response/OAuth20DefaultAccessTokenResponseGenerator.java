package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
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
public class OAuth20DefaultAccessTokenResponseGenerator<T extends OAuth20ConfigurationContext> implements OAuth20AccessTokenResponseGenerator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    protected final ObjectProvider<T> configurationContext;

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
    public ModelAndView generate(final OAuth20AccessTokenResponseResult result) {
        if (shouldGenerateDeviceFlowResponse(result)) {
            return generateResponseForDeviceToken(result);
        }
        return generateResponseForAccessToken(result);
    }

    protected ModelAndView generateResponseForDeviceToken(final OAuth20AccessTokenResponseResult result) {
        val model = getDeviceTokenResponseModel(result);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    protected Map getDeviceTokenResponseModel(final OAuth20AccessTokenResponseResult result) {
        val model = new LinkedHashMap<String, Object>();
        val uri = result.getCasProperties().getServer().getPrefix()
            .concat(OAuth20Constants.BASE_OAUTH20_URL)
            .concat("/")
            .concat(OAuth20Constants.DEVICE_AUTHZ_URL);
        model.put(OAuth20Constants.DEVICE_VERIFICATION_URI, uri);
        model.put(OAuth20Constants.EXPIRES_IN, result.getDeviceTokenTimeout());
        val generatedToken = result.getGeneratedToken();
        generatedToken.getUserCode().ifPresent(userCode -> model.put(OAuth20Constants.DEVICE_USER_CODE, userCode));
        generatedToken.getDeviceCode().ifPresent(deviceCode -> model.put(OAuth20Constants.DEVICE_CODE, deviceCode));
        model.put(OAuth20Constants.DEVICE_INTERVAL, result.getDeviceRefreshInterval());
        return model;
    }

    protected ModelAndView generateResponseForAccessToken(final OAuth20AccessTokenResponseResult result) {
        val model = getAccessTokenResponseModel(result);
        val modelAndView = new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
        modelAndView.setStatus(HttpStatus.OK);
        return modelAndView;
    }

    protected Map<String, Object> getAccessTokenResponseModel(final OAuth20AccessTokenResponseResult result) {
        val model = new LinkedHashMap<String, Object>();
        val generatedToken = result.getGeneratedToken();
        generatedToken.getAccessToken()
            .ifPresent(token -> {
                val accessToken = resolveToken(token, OAuth20AccessToken.class);
                if (result.getResponseType() != OAuth20ResponseTypes.ID_TOKEN && accessToken.getExpiresIn() > 0) {
                    val encodedAccessTokenId = encodeOAuthToken(accessToken, result);
                    if (Strings.CI.equals(encodedAccessTokenId, accessToken.getId()) && token.isStateless()) {
                        model.put(OAuth20Constants.ACCESS_TOKEN, token.getId());
                    } else {
                        model.put(OAuth20Constants.ACCESS_TOKEN, encodedAccessTokenId);
                    }

                    if (!accessToken.getScopes().isEmpty()) {
                        model.put(OAuth20Constants.SCOPE, String.join(" ", accessToken.getScopes()));
                    }
                    model.put(OAuth20Constants.EXPIRES_IN, accessToken.getExpiresIn());
                    val authentication = accessToken.getAuthentication();
                    model.put(OAuth20Constants.TOKEN_TYPE, authentication.containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)
                        ? OAuth20Constants.TOKEN_TYPE_DPOP : OAuth20Constants.TOKEN_TYPE_BEARER);
                    if (result.getUserProfile() != null) {
                        result.getUserProfile().addAttribute(Principal.class.getName(), authentication.getPrincipal());
                    }
                    if (result.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE) {
                        model.put(OAuth20Constants.ISSUED_TOKEN_TYPE, result.getRequestedTokenType().getType());
                    }
                }
            });
        generatedToken.getRefreshToken().ifPresent(ticket -> {
            val refreshToken = resolveToken(ticket, OAuth20RefreshToken.class);
            val encodedRefreshToken = encodeOAuthToken(refreshToken, result);

            if (Strings.CI.equals(encodedRefreshToken, refreshToken.getId()) && ticket.isStateless()) {
                model.put(OAuth20Constants.REFRESH_TOKEN, ticket.getId());
            } else {
                model.put(OAuth20Constants.REFRESH_TOKEN, encodedRefreshToken);
            }
        });
        return model;
    }

    protected <TokenType extends OAuth20Token> TokenType resolveToken(final Ticket token, final Class<TokenType> clazz) {
        return token == null
            ? null
            : (token.isStateless() ? configurationContext.getObject().getTicketRegistry().getTicket(token.getId(), clazz) : (TokenType) token);
    }

    protected String encodeOAuthToken(final OAuth20Token token,
                                      final OAuth20AccessTokenResponseResult result) {
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext.getObject(), result, token);
        return cipher.encode(token.getId(), new Object[]{token, result});
    }
}
