package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
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
public class OAuth20DefaultAccessTokenResponseGenerator implements OAuth20AccessTokenResponseGenerator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    protected final JwtBuilder accessTokenJwtBuilder;
    protected final TicketRegistry ticketRegistry;
    protected final CasConfigurationProperties casProperties;

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
        generatedToken.getAccessToken().ifPresent(token -> {
            val accessToken = resolveAccessToken(token);
            if (accessToken.getExpiresIn() > 0) {
                val encodedAccessTokenId = encodeAccessToken(accessToken, result);
                if (StringUtils.equals(encodedAccessTokenId, accessToken.getId()) && token.isStateless()) {
                    model.put(OAuth20Constants.ACCESS_TOKEN, token.getId());
                } else {
                    model.put(OAuth20Constants.ACCESS_TOKEN, encodedAccessTokenId);
                }

                model.put(OAuth20Constants.SCOPE, String.join(" ", accessToken.getScopes()));
                model.put(OAuth20Constants.EXPIRES_IN, accessToken.getExpiresIn());
                val authentication = accessToken.getAuthentication();
                model.put(OAuth20Constants.TOKEN_TYPE, authentication.containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)
                    ? OAuth20Constants.TOKEN_TYPE_DPOP : OAuth20Constants.TOKEN_TYPE_BEARER);
                if (result.getUserProfile() != null) {
                    result.getUserProfile().addAttribute(Principal.class.getName(), authentication.getPrincipal());
                }
            }
        });
        generatedToken.getRefreshToken().ifPresent(rt -> model.put(OAuth20Constants.REFRESH_TOKEN, rt.getId()));
        return model;
    }

    protected OAuth20AccessToken resolveAccessToken(final Ticket token) {
        return (OAuth20AccessToken) (token.isStateless() ? ticketRegistry.getTicket(token.getId()) : token);
    }

    protected String encodeAccessToken(final OAuth20AccessToken accessToken,
                                       final OAuth20AccessTokenResponseResult result) {
        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(accessTokenJwtBuilder,
            result.getRegisteredService(), accessToken, result.getService(), casProperties);
        return cipher.encode(accessToken.getId(), new Object[]{accessToken, result});
    }
}
