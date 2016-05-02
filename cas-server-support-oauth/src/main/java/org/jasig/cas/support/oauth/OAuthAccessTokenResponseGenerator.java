package org.jasig.cas.support.oauth;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuthAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("oauthAccessTokenResponseGenerator")
public class OAuthAccessTokenResponseGenerator implements AccessTokenResponseGenerator {
    /**
     * The JSON factory.
     */
    protected final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @Override
    public void generate(final HttpServletResponse response,
                         final OAuthRegisteredService registeredService,
                         final Service service,
                         final AccessToken accessTokenId,
                         final RefreshToken refreshTokenId,
                         final long timeout) {

        if (registeredService.isJsonFormat()) {
            response.setContentType("application/json");
            try (final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(response.getWriter())) {
                jsonGenerator.writeStartObject();
                generateJsonInternal(jsonGenerator, accessTokenId, refreshTokenId, timeout, service, registeredService);
                jsonGenerator.writeEndObject();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            generateTextInternal(response, accessTokenId, refreshTokenId, timeout);
        }
    }

    /**
     * Generate text internal.
     *
     * @param response       the response
     * @param accessTokenId  the access token id
     * @param refreshTokenId the refresh token id
     * @param timeout        the timeout
     */
    protected void generateTextInternal(final HttpServletResponse response,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout) {
        String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessTokenId.getId(),
                OAuthConstants.EXPIRES, timeout);
        if (refreshTokenId != null) {
            text += '&' + OAuthConstants.REFRESH_TOKEN + '=' + refreshTokenId.getId();
        }
        OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
    }

    /**
     * Generate internal.
     *
     * @param jsonGenerator     the json generator
     * @param accessTokenId     the access token id
     * @param refreshTokenId    the refresh token id
     * @param timeout           the timeout
     * @param service           the service
     * @param registeredService the registered service
     * @throws Exception the exception
     */
    protected void generateJsonInternal(final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService) throws Exception {
        jsonGenerator.writeStringField(OAuthConstants.ACCESS_TOKEN, accessTokenId.getId());
        jsonGenerator.writeNumberField(OAuthConstants.EXPIRES, timeout);
        if (refreshTokenId != null) {
            jsonGenerator.writeStringField(OAuthConstants.REFRESH_TOKEN, refreshTokenId.getId());
        }
    }
}
