package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.NameValuePair;
import org.apereo.inspektr.audit.annotation.Audit;
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
    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    public ModelAndView build(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        LOGGER.debug("Authorize request verification successful for client [{}] with redirect uri [{}]",
            tokenRequestContext.getClientId(), tokenRequestContext.getRedirectUri());
        val result = configurationContext.getAccessTokenGenerator().generate(tokenRequestContext);
        val accessToken = result.getAccessToken().orElseThrow();
        val refreshToken = result.getRefreshToken().orElse(null);
        LOGGER.debug("Generated OAuth access token: [{}]", accessToken);
        return buildCallbackUrlResponseType(tokenRequestContext, accessToken, refreshToken, new ArrayList<>());
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return Strings.CI.equals(context.getResponseType(), OAuth20ResponseTypes.TOKEN.getType());
    }

    protected ModelAndView buildCallbackUrlResponseType(final AccessTokenRequestContext tokenRequestContext,
                                                        final Ticket givenAccessToken, final Ticket givenRefreshToken,
                                                        final List<NameValuePair> parameters) throws Throwable {
        val attributes = tokenRequestContext.getAuthentication().getAttributes();
        
        val accessToken = resolveAccessToken(givenAccessToken);
        val paramsToBuild = new LinkedHashMap<String, String>();

        parameters.forEach(nvp -> paramsToBuild.put(nvp.getName(), nvp.getValue()));
        
        if (includeAccessTokenInResponse(tokenRequestContext) && accessToken.getExpiresIn() > 0) {
            val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext,
                tokenRequestContext.getRegisteredService(), accessToken,
                tokenRequestContext.getService(), false);
            val encodedAccessToken = cipher.encode(accessToken.getId());
            paramsToBuild.put(OAuth20Constants.ACCESS_TOKEN, encodedAccessToken);
            paramsToBuild.put(OAuth20Constants.TOKEN_TYPE, OAuth20Constants.TOKEN_TYPE_BEARER);
            paramsToBuild.put(OAuth20Constants.EXPIRES_IN, String.valueOf(accessToken.getExpiresIn()));
        }

        if (givenRefreshToken != null) {
            val refreshToken = resolveRefreshToken(givenRefreshToken);
            paramsToBuild.put(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        }

        CollectionUtils.firstElement(attributes.get(OAuth20Constants.STATE))
            .ifPresent(state -> paramsToBuild.put(OAuth20Constants.STATE, state.toString()));
        CollectionUtils.firstElement(attributes.get(OAuth20Constants.NONCE))
            .ifPresent(nonce -> paramsToBuild.put(OAuth20Constants.NONCE, nonce.toString()));
        
        LOGGER.debug("Redirecting to URL [{}]", tokenRequestContext.getRedirectUri());
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), accessToken.getClientId());
        var responseMode = tokenRequestContext.getResponseMode();
        if (responseMode == null || responseMode == OAuth20ResponseModeTypes.NONE) {
            responseMode = OAuth20ResponseModeTypes.FRAGMENT;
        }
        return build(registeredService, responseMode, tokenRequestContext.getRedirectUri(), paramsToBuild);
    }

    protected boolean includeAccessTokenInResponse(final AccessTokenRequestContext tokenRequestContext) {
        return true;
    }
}
