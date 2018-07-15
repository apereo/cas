package org.apereo.cas.oidc.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;

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
@RequiredArgsConstructor
public class OidcAccessTokenResponseGenerator extends OAuth20AccessTokenResponseGenerator {
    private final OidcIdTokenGeneratorService idTokenGenerator;

    @Override
    protected Map getAccessTokenResponseModel(final HttpServletRequest request, final HttpServletResponse response, final AccessToken accessTokenId,
                                              final RefreshToken refreshTokenId, final long timeout, final Service service,
                                              final OAuthRegisteredService registeredService, final OAuth20ResponseTypes responseType) throws Exception {
        val model = super.getAccessTokenResponseModel(request, response, accessTokenId, refreshTokenId, timeout, service, registeredService, responseType);
        val oidcRegisteredService = (OidcRegisteredService) registeredService;
        val idToken = this.idTokenGenerator.generate(request, response, accessTokenId,
            timeout, responseType, oidcRegisteredService);
        model.put(OidcConstants.ID_TOKEN, idToken);
        return model;
    }
}

