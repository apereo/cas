package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.pac4j.core.context.WebContext;
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

    public OidcImplicitIdTokenAuthorizationResponseBuilder(final T configurationContext,
                                                           final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public boolean supports(final WebContext context) {
        val responseType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.ID_TOKEN);
    }

    @Override
    protected ModelAndView buildCallbackUrlResponseType(
        final AccessTokenRequestDataHolder holder,
        final String redirectUri,
        final OAuth20AccessToken accessToken,
        final List<NameValuePair> params,
        final OAuth20RefreshToken refreshToken) throws Exception {
        val idToken = configurationContext.getIdTokenGeneratorService().generate(accessToken,
            configurationContext.getIdTokenExpirationPolicy().buildTicketExpirationPolicy().getTimeToLive(),
            OAuth20ResponseTypes.ID_TOKEN, holder.getGrantType(), holder.getRegisteredService());
        LOGGER.debug("Generated id token [{}]", idToken);
        params.add(new BasicNameValuePair(OidcConstants.ID_TOKEN, idToken));
        return super.buildCallbackUrlResponseType(holder, redirectUri, accessToken, params, refreshToken);
    }
}
