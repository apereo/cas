package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * This is {@link OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder<T extends OidcConfigurationContext> extends OAuth20TokenAuthorizationResponseBuilder<T> {

    public OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return OAuth20Utils.isResponseType(context.getResponseType(), OAuth20ResponseTypes.IDTOKEN_TOKEN);
    }

    @Override
    protected ModelAndView buildCallbackUrlResponseType(
        final AccessTokenRequestContext holder,
        final OAuth20AccessToken accessToken,
        final List<NameValuePair> params,
        final OAuth20RefreshToken refreshToken) throws Exception {
        val idToken = configurationContext.getIdTokenGeneratorService().generate(accessToken,
            configurationContext.getIdTokenExpirationPolicy().buildTicketExpirationPolicy().getTimeToLive(),
            holder.getUserProfile(), OAuth20ResponseTypes.IDTOKEN_TOKEN, holder.getGrantType(),
            holder.getRegisteredService());
        LOGGER.debug("Generated id token [{}]", idToken);
        params.add(new BasicNameValuePair(OidcConstants.ID_TOKEN, idToken));
        return super.buildCallbackUrlResponseType(holder, accessToken, params, refreshToken);
    }
}
