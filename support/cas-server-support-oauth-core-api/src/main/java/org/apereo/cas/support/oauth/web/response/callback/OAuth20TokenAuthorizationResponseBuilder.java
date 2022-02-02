package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link OAuth20TokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class OAuth20TokenAuthorizationResponseBuilder<T extends OAuth20ConfigurationContext> extends BaseOAuth20AuthorizationResponseBuilder<T> {
    public OAuth20TokenAuthorizationResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public ModelAndView build(final AccessTokenRequestContext holder) throws Exception {
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", holder.getClientId(), holder.getRedirectUri());
        val result = configurationContext.getAccessTokenGenerator().generate(holder);
        val accessToken = result.getAccessToken().orElse(null);
        val refreshToken = result.getRefreshToken().orElse(null);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(holder, accessToken, new ArrayList<>(0), refreshToken);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return StringUtils.equalsIgnoreCase(context.getResponseType(), OAuth20ResponseTypes.TOKEN.getType());
    }

    /**
     * Build callback url response type string.
     *
     * @param holder       the holder
     * @param accessToken  the access token
     * @param params       the params
     * @param refreshToken the refresh token
     * @return the string
     * @throws Exception the exception
     */
    protected ModelAndView buildCallbackUrlResponseType(
        final AccessTokenRequestContext holder,
        final OAuth20AccessToken accessToken,
        final List<NameValuePair> params,
        final OAuth20RefreshToken refreshToken) throws Exception {
        val attributes = holder.getAuthentication().getAttributes();
        val state = attributes.get(OAuth20Constants.STATE).get(0).toString();
        val nonce = attributes.get(OAuth20Constants.NONCE).get(0).toString();

        val builder = new URIBuilder(holder.getRedirectUri());
        val stringBuilder = new StringBuilder();

        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenJwtBuilder(configurationContext.getAccessTokenJwtBuilder())
            .casProperties(configurationContext.getCasProperties())
            .build()
            .encode();

        val expiresIn = accessToken.getExpiresIn();
        stringBuilder.append(OAuth20Constants.ACCESS_TOKEN)
            .append('=')
            .append(encodedAccessToken)
            .append('&')
            .append(OAuth20Constants.TOKEN_TYPE)
            .append('=')
            .append(OAuth20Constants.TOKEN_TYPE_BEARER)
            .append('&')
            .append(OAuth20Constants.EXPIRES_IN)
            .append('=')
            .append(expiresIn);

        if (refreshToken != null) {
            stringBuilder.append('&')
                .append(OAuth20Constants.REFRESH_TOKEN)
                .append('=')
                .append(refreshToken.getId());
        }

        params.forEach(p -> stringBuilder.append('&')
            .append(p.getName())
            .append('=')
            .append(p.getValue()));

        if (StringUtils.isNotBlank(state)) {
            stringBuilder.append('&')
                .append(OAuth20Constants.STATE)
                .append('=')
                .append(state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            stringBuilder.append('&')
                .append(OAuth20Constants.NONCE)
                .append('=')
                .append(nonce);
        }
        builder.setFragment(stringBuilder.toString());
        val url = builder.toString();

        LOGGER.debug("Redirecting to URL [{}]", url);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), accessToken.getClientId());
        return build(registeredService, holder.getResponseMode(), url, new LinkedHashMap<>());
    }
}
