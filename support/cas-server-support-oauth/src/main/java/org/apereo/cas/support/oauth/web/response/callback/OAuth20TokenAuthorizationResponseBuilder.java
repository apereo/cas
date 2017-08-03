package org.apereo.cas.support.oauth.web.response.callback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.util.EncodingUtils;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OAuth20TokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20TokenAuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20TokenAuthorizationResponseBuilder.class);
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final ExpirationPolicy accessTokenExpirationPolicy;

    public OAuth20TokenAuthorizationResponseBuilder(final OAuth20TokenGenerator accessTokenGenerator,
                                                    final ExpirationPolicy accessTokenExpirationPolicy) {
        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenExpirationPolicy = accessTokenExpirationPolicy;
    }

    @Override
    public View build(final J2EContext context, final String clientId, final AccessTokenRequestDataHolder holder) {
        try {
            final String redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
            LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);
            final Pair<AccessToken, RefreshToken> accessToken = accessTokenGenerator.generate(holder);
            LOGGER.debug("Generated OAuth access token: [{}]", accessToken.getKey());
            return buildCallbackUrlResponseType(holder, redirectUri, accessToken.getKey(), new ArrayList<>(), accessToken.getValue(), context);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Build callback url response type string.
     *
     * @param holder       the holder
     * @param redirectUri  the redirect uri
     * @param accessToken  the access token
     * @param params       the params
     * @param refreshToken the refresh token
     * @param context      the context
     * @return the string
     * @throws Exception the exception
     */
    protected View buildCallbackUrlResponseType(final AccessTokenRequestDataHolder holder,
                                                final String redirectUri,
                                                final AccessToken accessToken,
                                                final List<NameValuePair> params,
                                                final RefreshToken refreshToken,
                                                final J2EContext context) throws Exception {
        final String state = holder.getAuthentication().getAttributes().get(OAuth20Constants.STATE).toString();
        final String nonce = holder.getAuthentication().getAttributes().get(OAuth20Constants.NONCE).toString();

        final URIBuilder builder = new URIBuilder(redirectUri);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OAuth20Constants.ACCESS_TOKEN)
                .append('=')
                .append(accessToken.getId())
                .append('&')
                .append(OAuth20Constants.TOKEN_TYPE)
                .append('=')
                .append(OAuth20Constants.TOKEN_TYPE_BEARER)
                .append('&')
                .append(OAuth20Constants.EXPIRES_IN)
                .append('=')
                .append(accessTokenExpirationPolicy.getTimeToLive());

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
                    .append(EncodingUtils.urlEncode(state));
        }
        if (StringUtils.isNotBlank(nonce)) {
            stringBuilder.append('&')
                    .append(OAuth20Constants.NONCE)
                    .append('=')
                    .append(EncodingUtils.urlEncode(nonce));
        }
        builder.setFragment(stringBuilder.toString());
        final String url = builder.toString();

        LOGGER.debug("Redirecting to URL [{}]", url);
        return new RedirectView(url);
    }

    @Override
    public boolean supports(final J2EContext context) {
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.TOKEN.getType());
    }
}
