package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * This is {@link OidcImplicitIdTokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcImplicitIdTokenAuthorizationResponseBuilder<T extends OidcConfigurationContext> extends OAuth20TokenAuthorizationResponseBuilder<T> {

    public OidcImplicitIdTokenAuthorizationResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return OAuth20Utils.isResponseType(context.getResponseType(), OAuth20ResponseTypes.ID_TOKEN);
    }

    @Override
    protected ModelAndView buildCallbackUrlResponseType(final AccessTokenRequestContext tokenRequestContext,
                                                        final Ticket givenAccessToken, final Ticket givenRefreshToken,
                                                        final List<NameValuePair> parameters) throws Throwable {
        val accessToken = resolveAccessToken(givenAccessToken);
        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(tokenRequestContext.getUserProfile())
            .responseType(OAuth20ResponseTypes.ID_TOKEN)
            .grantType(tokenRequestContext.getGrantType())
            .registeredService(tokenRequestContext.getRegisteredService())
            .build();
        
        val idToken = configurationContext.getIdTokenGeneratorService().generate(idTokenContext);
        if (idToken != null) {
            LOGGER.debug("Generated ID token [{}]", idToken);
            parameters.add(new BasicNameValuePair(OidcConstants.ID_TOKEN, idToken.token()));
        }
        return super.buildCallbackUrlResponseType(tokenRequestContext, accessToken, givenRefreshToken, parameters);
    }

    @Override
    protected boolean includeAccessTokenInResponse(final AccessTokenRequestContext tokenRequestContext) {
        return false;
    }
}
