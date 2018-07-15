package org.apereo.cas.support.oauth.web.response.accesstoken;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OAuth20AccessTokenResponseGenerator implements AccessTokenResponseGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * The Resource loader.
     */
    @Autowired
    protected ResourceLoader resourceLoader;

    @Override
    @SneakyThrows
    public ModelAndView generate(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final OAuthRegisteredService registeredService,
                                 final Service service,
                                 final OAuth20TokenGeneratedResult result,
                                 final long accessTokenTimeout,
                                 final OAuth20ResponseTypes responseType,
                                 final CasConfigurationProperties casProperties) {

        if (OAuth20ResponseTypes.DEVICE_CODE == responseType) {
            return generateResponseForDeviceToken(request, response, registeredService, service, result, casProperties);
        }

        return generateResponseForAccessToken(request, response, registeredService, service, result, accessTokenTimeout, responseType);
    }

    /**
     * Generate response for device token model and view.
     *
     * @param request           the request
     * @param response          the response
     * @param registeredService the registered service
     * @param service           the service
     * @param result            the result
     * @param casProperties     the cas properties
     * @return the model and view
     */
    @SneakyThrows
    protected ModelAndView generateResponseForDeviceToken(final HttpServletRequest request,
                                                          final HttpServletResponse response,
                                                          final OAuthRegisteredService registeredService,
                                                          final Service service,
                                                          final OAuth20TokenGeneratedResult result,
                                                          final CasConfigurationProperties casProperties) {
        val model = getDeviceTokenResponseModel(result, casProperties);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    /**
     * Gets device token response model.
     *
     * @param result        the result
     * @param casProperties the cas properties
     * @return the device token response model
     */
    protected Map getDeviceTokenResponseModel(final OAuth20TokenGeneratedResult result, final CasConfigurationProperties casProperties) {
        val model = new LinkedHashMap<>();
        val uri = casProperties.getServer().getPrefix()
            .concat(OAuth20Constants.BASE_OAUTH20_URL)
            .concat("/")
            .concat(OAuth20Constants.DEVICE_AUTHZ_URL);
        model.put(OAuth20Constants.DEVICE_VERIFICATION_URI, uri);
        model.put(OAuth20Constants.DEVICE_USER_CODE, result.getUserCode().get());
        model.put(OAuth20Constants.DEVICE_CODE, result.getDeviceCode().get());

        val deviceRefreshInterval = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getRefreshInterval()).getSeconds();
        model.put(OAuth20Constants.DEVICE_INTERVAL, deviceRefreshInterval);
        return model;
    }

    /**
     * Generate response for access token model and view.
     *
     * @param request           the request
     * @param response          the response
     * @param registeredService the registered service
     * @param service           the service
     * @param result            the result
     * @param timeout           the timeout
     * @param responseType      the response type
     * @return the model and view
     * @throws Exception the exception
     */
    protected ModelAndView generateResponseForAccessToken(final HttpServletRequest request, final HttpServletResponse response,
                                                          final OAuthRegisteredService registeredService, final Service service,
                                                          final OAuth20TokenGeneratedResult result, final long timeout,
                                                          final OAuth20ResponseTypes responseType) throws Exception {
        val accessToken = result.getAccessToken().get();
        val refreshToken = result.getRefreshToken().get();
        val model = getAccessTokenResponseModel(request, response, accessToken, refreshToken, timeout, service, registeredService, responseType);
        return new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
    }

    /**
     * Generate internal.
     *
     * @param request           the request
     * @param response          the response
     * @param accessTokenId     the access token id
     * @param refreshTokenId    the refresh token id
     * @param timeout           the timeout
     * @param service           the service
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return the access token response model
     * @throws Exception the exception
     */
    protected Map getAccessTokenResponseModel(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final AccessToken accessTokenId,
                                              final RefreshToken refreshTokenId,
                                              final long timeout,
                                              final Service service,
                                              final OAuthRegisteredService registeredService,
                                              final OAuth20ResponseTypes responseType) throws Exception {
        val model = new LinkedHashMap<>();

        model.put(OAuth20Constants.ACCESS_TOKEN, accessTokenId.getId());
        model.put(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
        model.put(OAuth20Constants.EXPIRES_IN, timeout);
        if (refreshTokenId != null) {
            model.put(OAuth20Constants.REFRESH_TOKEN, refreshTokenId.getId());
        }
        return model;
    }
}
