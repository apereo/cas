package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.token.JwtBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.context.JEEContext;
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
@RequiredArgsConstructor
@Getter
public class OAuth20TokenAuthorizationResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final ExpirationPolicyBuilder<OAuth20AccessToken> accessTokenExpirationPolicy;
    private final ServicesManager servicesManager;
    private final JwtBuilder accessTokenJwtBuilder;
    private final CasConfigurationProperties casProperties;

    @Override
    @SneakyThrows
    public ModelAndView build(final JEEContext context,
                              final String clientId,
                              final AccessTokenRequestDataHolder holder) {

        val redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);
        val result = accessTokenGenerator.generate(holder);
        val accessToken = result.getAccessToken().orElse(null);
        val refreshToken = result.getRefreshToken().orElse(null);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(holder, redirectUri, accessToken, new ArrayList<>(0), refreshToken, context);
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
    protected ModelAndView buildCallbackUrlResponseType(final AccessTokenRequestDataHolder holder,
                                                        final String redirectUri,
                                                        final OAuth20AccessToken accessToken,
                                                        final List<NameValuePair> params,
                                                        final OAuth20RefreshToken refreshToken,
                                                        final JEEContext context) throws Exception {
        val attributes = holder.getAuthentication().getAttributes();
        val state = attributes.get(OAuth20Constants.STATE).get(0).toString();
        val nonce = attributes.get(OAuth20Constants.NONCE).get(0).toString();

        val builder = new URIBuilder(redirectUri);
        val stringBuilder = new StringBuilder();

        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
            .accessToken(accessToken)
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .casProperties(casProperties)
            .build()
            .encode();

        val expiration = accessTokenExpirationPolicy.buildTicketExpirationPolicy();
        val timeToLive = expiration.getTimeToLive();
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
            .append(timeToLive);

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
        val parameters = new LinkedHashMap<String, String>();
        parameters.put(OAuth20Constants.ACCESS_TOKEN, encodedAccessToken);
        if (refreshToken != null) {
            parameters.put(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        }
        parameters.put(OAuth20Constants.EXPIRES_IN, timeToLive.toString());
        parameters.put(OAuth20Constants.STATE, state);
        parameters.put(OAuth20Constants.NONCE, nonce);
        parameters.put(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        return buildResponseModelAndView(context, servicesManager, accessToken.getClientId(), url, parameters);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.TOKEN.getType());
    }
}
