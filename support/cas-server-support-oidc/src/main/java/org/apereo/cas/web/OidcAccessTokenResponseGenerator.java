package org.apereo.cas.web;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.OidcIdTokenGenerator;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAccessTokenResponseGenerator extends OAuth20AccessTokenResponseGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAccessTokenResponseGenerator.class);
    private final OidcIdTokenGenerator idTokenGenerator;

    public OidcAccessTokenResponseGenerator(final OidcIdTokenGenerator idTokenGenerator) {
        this.idTokenGenerator = idTokenGenerator;
    }

    @Override
    protected void generateJsonInternal(final HttpServletRequest request, final HttpServletResponse response,
                                        final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId, final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService) throws Exception {

        super.generateJsonInternal(request, response, jsonGenerator, accessTokenId,
                refreshTokenId, timeout, service, registeredService);
        final OidcRegisteredService oidcRegisteredService = (OidcRegisteredService) registeredService;
        final String idToken = this.idTokenGenerator.generate(request, response, accessTokenId,
                timeout, oidcRegisteredService);
        jsonGenerator.writeStringField(OidcConstants.ID_TOKEN, idToken);
    }

}

