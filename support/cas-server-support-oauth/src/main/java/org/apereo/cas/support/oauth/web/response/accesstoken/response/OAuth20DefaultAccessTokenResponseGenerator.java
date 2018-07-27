package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DefaultAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20DefaultAccessTokenResponseGenerator implements OAuth20AccessTokenResponseGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Audit(action = "OAUTH2_ACCESS_TOKEN_RESPONSE",
        actionResolverName = "OAUTH2_ACCESS_TOKEN_RESPONSE_ACTION_RESOLVER",
        resourceResolverName = "OAUTH2_ACCESS_TOKEN_RESPONSE_RESOURCE_RESOLVER")
    @Override
    @SneakyThrows
    public ModelAndView generate(final HttpServletRequest request, final HttpServletResponse response,
                                 final OAuth20AccessTokenResponseResult result) {
        if (shouldGenerateDeviceFlowResponse(result)) {
            return generateResponseForDeviceToken(request, response, result);
        }

        return generateResponseForAccessToken(request, response, result);
    }

    private boolean shouldGenerateDeviceFlowResponse(final OAuth20AccessTokenResponseResult result) {
        val generatedToken = result.getGeneratedToken();
        return OAuth20ResponseTypes.DEVICE_CODE == result.getResponseType()
            && generatedToken.getDeviceCode().isPresent()
            && generatedToken.getUserCode().isPresent()
            && !generatedToken.getAccessToken().isPresent();
    }

    /**
     * Generate response for device token model and view.
     *
     * @param request  the request
     * @param response the response
     * @param result   the result
     * @return the model and view
     */
    @SneakyThrows
    protected ModelAndView generateResponseForDeviceToken(final HttpServletRequest request,
                                                          final HttpServletResponse response,
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
        val model = new LinkedHashMap<>();
        val uri = result.getCasProperties().getServer().getPrefix()
            .concat(OAuth20Constants.BASE_OAUTH20_URL)
            .concat("/")
            .concat(OAuth20Constants.DEVICE_AUTHZ_URL);
        model.put(OAuth20Constants.DEVICE_VERIFICATION_URI, uri);
        model.put(OAuth20Constants.EXPIRES_IN, result.getDeviceTokenTimeout());
        result.getGeneratedToken().getUserCode().ifPresent(c -> model.put(OAuth20Constants.DEVICE_USER_CODE, c));
        result.getGeneratedToken().getDeviceCode().ifPresent(c -> model.put(OAuth20Constants.DEVICE_CODE, c));
        model.put(OAuth20Constants.DEVICE_INTERVAL, result.getDeviceRefreshInterval());
        return model;
    }

    /**
     * Generate response for access token model and view.
     *
     * @param request  the request
     * @param response the response
     * @param result   the result
     * @return the model and view
     */
    protected ModelAndView generateResponseForAccessToken(final HttpServletRequest request, final HttpServletResponse response,
                                                          final OAuth20AccessTokenResponseResult result) {
        val model = getAccessTokenResponseModel(request, response, result);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    /**
     * Generate internal.
     *
     * @param request  the request
     * @param response the response
     * @param result   the result
     * @return the access token response model
     */
    protected Map getAccessTokenResponseModel(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final OAuth20AccessTokenResponseResult result) {
        val model = new LinkedHashMap<>();
        result.getGeneratedToken().getAccessToken().ifPresent(t -> model.put(OAuth20Constants.ACCESS_TOKEN, t.getId()));
        result.getGeneratedToken().getRefreshToken().ifPresent(t -> model.put(OAuth20Constants.REFRESH_TOKEN, t.getId()));
        model.put(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
        model.put(OAuth20Constants.EXPIRES_IN, result.getAccessTokenTimeout());
        return model;
    }
}
