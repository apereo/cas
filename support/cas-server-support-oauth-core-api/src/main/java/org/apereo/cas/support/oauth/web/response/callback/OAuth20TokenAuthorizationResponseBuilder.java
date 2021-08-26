package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.token.JwtBuilder;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.context.WebContext;
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
public class OAuth20TokenAuthorizationResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder {
    private final OAuth20TokenGenerator accessTokenGenerator;

    private final JwtBuilder accessTokenJwtBuilder;

    public OAuth20TokenAuthorizationResponseBuilder(final ServicesManager servicesManager,
                                                    final CasConfigurationProperties casProperties,
                                                    final OAuth20TokenGenerator accessTokenGenerator,
                                                    final JwtBuilder accessTokenJwtBuilder,
                                                    final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(servicesManager, casProperties, authorizationModelAndViewBuilder);
        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenJwtBuilder = accessTokenJwtBuilder;
    }

    @Override
    @SneakyThrows
    public ModelAndView build(final WebContext context,
                              final String clientId,
                              final AccessTokenRequestDataHolder holder) {

        val redirectUri = OAuth20Utils.getRequestParameter(context, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);
        val result = accessTokenGenerator.generate(holder);
        val accessToken = result.getAccessToken().orElse(null);
        val refreshToken = result.getRefreshToken().orElse(null);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(holder, redirectUri, accessToken, new ArrayList<>(0), refreshToken, context);
    }

    @Override
    public boolean supports(final WebContext context) {
        val responseType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.TOKEN.getType());
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
                                                        final WebContext context) throws Exception {
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
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
        return build(context, registeredService, url, new LinkedHashMap<>());
    }
}
