package org.apereo.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20AccessTokenResponseGenerator implements AccessTokenResponseGenerator {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
                         final long timeout) {

        if (registeredService.isJsonFormat()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
            try(JsonGenerator jsonGenerator = jsonFactory.createGenerator(response.getWriter())) {
                jsonGenerator.writeStartObject();
                generateJsonInternal(request, response, jsonGenerator, accessTokenId,
                        refreshTokenId, timeout, service, registeredService);
                jsonGenerator.writeEndObject();
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            generateTextInternal(request, response, accessTokenId, refreshTokenId, timeout);
        }
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
                String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessTokenId.getId(),
                        OAuthConstants.EXPIRES_IN, timeout));

        if (refreshTokenId != null) {
            builder.append('&')
                    .append(OAuthConstants.REFRESH_TOKEN)
                    .append('=')
                    .append(refreshTokenId.getId());
        }
        OAuthUtils.writeText(response, builder.toString(), HttpStatus.SC_OK);
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
     * @throws Exception the exception
     */
    protected void generateJsonInternal(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService) throws Exception {
        jsonGenerator.writeStringField(OAuthConstants.ACCESS_TOKEN, accessTokenId.getId());
        jsonGenerator.writeStringField(OAuthConstants.TOKEN_TYPE, OAuthConstants.TOKEN_TYPE_BEARER);
        jsonGenerator.writeNumberField(OAuthConstants.EXPIRES_IN, timeout);
        if (refreshTokenId != null) {
            jsonGenerator.writeStringField(OAuthConstants.REFRESH_TOKEN, refreshTokenId.getId());
        }
    }
}
