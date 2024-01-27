package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    public ModelAndView build(final AccessTokenRequestContext holder) throws Throwable {
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]", holder.getClientId(), holder.getRedirectUri());
        val result = configurationContext.getAccessTokenGenerator().generate(holder);
        val accessToken = result.getAccessToken().orElseThrow();
        val refreshToken = result.getRefreshToken().orElse(null);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(holder, accessToken, refreshToken, new ArrayList<>());
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return StringUtils.equalsIgnoreCase(context.getResponseType(), OAuth20ResponseTypes.TOKEN.getType());
    }

    protected ModelAndView buildCallbackUrlResponseType(final AccessTokenRequestContext tokenRequestContext,
        final Ticket givenAccessToken, final Ticket givenRefreshToken, final List<NameValuePair> parameters) throws Throwable {
        val attributes = tokenRequestContext.getAuthentication().getAttributes();
        val state = attributes.get(OAuth20Constants.STATE).getFirst().toString();
        val nonce = attributes.get(OAuth20Constants.NONCE).getFirst().toString();

        val accessToken = resolveAccessToken(givenAccessToken);
        val builder = new URIBuilder(tokenRequestContext.getRedirectUri());


        if (includeAccessTokenInResponse(tokenRequestContext)) {
            val encodedAccessToken = OAuth20JwtAccessTokenEncoder
                .builder()
                .accessToken(accessToken)
                .registeredService(tokenRequestContext.getRegisteredService())
                .service(tokenRequestContext.getService())
                .accessTokenJwtBuilder(configurationContext.getAccessTokenJwtBuilder())
                .casProperties(configurationContext.getCasProperties())
                .build()
                .encode(accessToken.getId());
            if (accessToken.getExpiresIn() > 0) {
                builder.addParameter(OAuth20Constants.ACCESS_TOKEN, encodedAccessToken);
                builder.addParameter(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
                builder.addParameter(OAuth20Constants.EXPIRES_IN, String.valueOf(accessToken.getExpiresIn()));
            }
        }

        if (givenRefreshToken != null) {
            val refreshToken = resolveRefreshToken(givenRefreshToken);
            builder.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        }
        FunctionUtils.doIfNotBlank(state, __ -> builder.addParameter(OAuth20Constants.STATE, state));
        FunctionUtils.doIfNotBlank(nonce, __ -> builder.addParameter(OAuth20Constants.NONCE, nonce));
        builder.addParameters(parameters);

        val parameterList = builder.getQueryParams()
            .stream()
            .map(parameter -> String.format("%s=%s", parameter.getName(), parameter.getValue()))
            .collect(Collectors.joining("&"));

        val url = UriComponentsBuilder.fromUriString(tokenRequestContext.getRedirectUri())
            .fragment(parameterList).build().toUriString();
        LOGGER.debug("Redirecting to URL [{}]", url);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), accessToken.getClientId());
        return build(registeredService, tokenRequestContext.getResponseMode(), url, new LinkedHashMap<>());
    }

    protected boolean includeAccessTokenInResponse(final AccessTokenRequestContext tokenRequestContext) {
        return true;
    }
}
