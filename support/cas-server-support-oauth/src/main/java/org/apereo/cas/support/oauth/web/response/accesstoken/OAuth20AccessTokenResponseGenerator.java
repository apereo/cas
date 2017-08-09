package org.apereo.cas.support.oauth.web.response.accesstoken;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is {@link OAuth20AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20AccessTokenResponseGenerator implements AccessTokenResponseGenerator {

    private static final JsonFactory JSON_FACTORY = new JsonFactory(new ObjectMapper().findAndRegisterModules());
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AccessTokenResponseGenerator.class);

    /**
     * The Resource loader.
     */
    @Autowired
    protected ResourceLoader resourceLoader;

    /**
     * CAS settings.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Override
    public void generate(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final OAuthRegisteredService registeredService,
                         final Service service,
                         final AccessToken accessTokenId,
                         final RefreshToken refreshTokenId,
                         final long timeout,
                         final OAuth20ResponseTypes responseType) {

        if (registeredService.isJsonFormat()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (JsonGenerator jsonGenerator = getResponseJsonGenerator(response)) {
                jsonGenerator.writeStartObject();
                generateJsonInternal(request, response, jsonGenerator, accessTokenId,
                        refreshTokenId, timeout, service, registeredService, responseType);
                jsonGenerator.writeEndObject();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            generateTextInternal(request, response, accessTokenId, refreshTokenId, timeout);
        }
    }

    /**
     * Gets response json generator.
     *
     * @param response the response
     * @return the response json generator
     * @throws IOException the io exception
     */
    protected JsonGenerator getResponseJsonGenerator(final HttpServletResponse response) throws IOException {
        return JSON_FACTORY.createGenerator(response.getWriter());
    }

    /**
     * Generate text internal.
     *
     * @param request        the request
     * @param response       the response
     * @param accessTokenId  the access token id
     * @param refreshTokenId the refresh token id
     * @param timeout        the timeout
     */
    protected void generateTextInternal(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout) {
        final StringBuilder builder = new StringBuilder(
                String.format("%s=%s&%s=%s", OAuth20Constants.ACCESS_TOKEN, accessTokenId.getId(),
                        OAuth20Constants.EXPIRES_IN, timeout));

        if (refreshTokenId != null) {
            builder.append('&')
                    .append(OAuth20Constants.REFRESH_TOKEN)
                    .append('=')
                    .append(refreshTokenId.getId());
        }
        OAuth20Utils.writeText(response, builder.toString(), HttpStatus.SC_OK);
    }

    /**
     * Generate internal.
     *
     * @param request           the request
     * @param response          the response
     * @param jsonGenerator     the json generator
     * @param accessTokenId     the access token id
     * @param refreshTokenId    the refresh token id
     * @param timeout           the timeout
     * @param service           the service
     * @param registeredService the registered service
     * @param responseType      the response type
     * @throws Exception the exception
     */
    protected void generateJsonInternal(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService,
                                        final OAuth20ResponseTypes responseType) throws Exception {
        jsonGenerator.writeStringField(OAuth20Constants.ACCESS_TOKEN, accessTokenId.getId());
        jsonGenerator.writeStringField(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
        jsonGenerator.writeNumberField(OAuth20Constants.EXPIRES_IN, timeout);
        if (refreshTokenId != null) {
            jsonGenerator.writeStringField(OAuth20Constants.REFRESH_TOKEN, refreshTokenId.getId());
        }
    }
}
