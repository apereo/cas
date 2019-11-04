package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20DefaultAccessTokenResponseGenerator;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.token.JwtBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcAccessTokenResponseGenerator extends OAuth20DefaultAccessTokenResponseGenerator {
    private final IdTokenGeneratorService idTokenGenerator;

    public OidcAccessTokenResponseGenerator(final IdTokenGeneratorService idTokenGenerator,
                                            final JwtBuilder jwtBuilder,
                                            final CasConfigurationProperties casProperties) {
        super(jwtBuilder, casProperties);
        this.idTokenGenerator = idTokenGenerator;
    }

    @Override
    protected Map<String, Object> getAccessTokenResponseModel(final HttpServletRequest request,
                                                              final HttpServletResponse response,
                                                              final OAuth20AccessTokenResponseResult result) {
        val model = super.getAccessTokenResponseModel(request, response, result);
        val accessToken = result.getGeneratedToken().getAccessToken();
        accessToken.ifPresent(token -> {
            val oidcRegisteredService = (OidcRegisteredService) result.getRegisteredService();
            val idToken = this.idTokenGenerator.generate(request, response, accessToken.get(),
                result.getAccessTokenTimeout(), result.getResponseType(), oidcRegisteredService);

            LOGGER.debug("Generated ID token [{}]", idToken);
            model.put(OidcConstants.ID_TOKEN, idToken);
        });
        return model;
    }
}

