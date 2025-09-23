package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.events.OAuth20AccessTokenResponseEvent;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.util.LoggingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.web.servlet.ModelAndView;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * This is {@link OAuth20AccessTokenResponseEncoder}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OAuth20AccessTokenResponseEncoder {
    private final OAuth20ConfigurationContext configurationContext;

    /**
     * Encode access token into model and view.
     *
     * @param tokenRequestContext the token request context
     * @param result              the result
     * @return the model and view
     */
    public ModelAndView encode(final AccessTokenRequestContext tokenRequestContext,
                               final OAuth20TokenGeneratedResult result) {
        LOGGER.debug("Generating access token response for [{}]", result);
        val deviceRefreshInterval = Beans.newDuration(getConfigurationContext().getCasProperties()
            .getAuthn().getOauth().getDeviceToken().getRefreshInterval()).toSeconds();
        val deviceTokenExpirationPolicy = getConfigurationContext().getDeviceTokenExpirationPolicy();
        val accessTokenTimeout = determineAccessTokenTimeoutInSeconds(result);

        val tokenResult = OAuth20AccessTokenResponseResult
            .builder()
            .registeredService(tokenRequestContext.getRegisteredService())
            .service(tokenRequestContext.getService())
            .accessTokenTimeout(accessTokenTimeout)
            .deviceRefreshInterval(deviceRefreshInterval)
            .deviceTokenTimeout(deviceTokenExpirationPolicy.buildTicketExpirationPolicyFor(
                tokenRequestContext.getRegisteredService()).getTimeToLive())
            .responseType(result.getResponseType().orElse(OAuth20ResponseTypes.NONE))
            .casProperties(getConfigurationContext().getCasProperties())
            .generatedToken(result)
            .grantType(result.getGrantType().orElse(OAuth20GrantTypes.NONE))
            .userProfile(tokenRequestContext.getUserProfile())
            .requestedTokenType(tokenRequestContext.getRequestedTokenType())
            .tokenExchangeAudience(tokenRequestContext.getTokenExchangeAudience())
            .tokenExchangeResource(tokenRequestContext.getTokenExchangeResource())
            .build();
        val generatedTokenResult = getConfigurationContext().getAccessTokenResponseGenerator().generate(tokenResult);
        val context = new LinkedHashMap<>(generatedTokenResult.getModel());
        if (generatedTokenResult.getStatus() != null) {
            context.put("status", generatedTokenResult.getStatus());
        }
        context.put(CasProtocolConstants.PARAMETER_SERVICE, tokenResult.getService().getId());
        Optional.ofNullable(tokenRequestContext.getToken()).ifPresent(token -> context.put(OAuth20Constants.TOKEN, token.getId()));
        context.put(OAuth20Constants.CLIENT_ID, tokenRequestContext.getRegisteredService().getClientId());
        context.put(OAuth20Constants.GRANT_TYPE, tokenRequestContext.getGrantType().getType());
        context.put(OAuth20Constants.RESPONSE_TYPE, tokenRequestContext.getResponseType().getType());
        LoggingUtils.protocolMessage("OAuth/OpenID Connect Token Response", context);
        configurationContext.getApplicationContext().publishEvent(
            new OAuth20AccessTokenResponseEvent(this, ClientInfoHolder.getClientInfo(), context));
        return generatedTokenResult;
    }

    protected Long determineAccessTokenTimeoutInSeconds(final OAuth20TokenGeneratedResult accessTokenResult) {
        return OAuth20Utils.getAccessTokenTimeout(accessTokenResult);
    }
}
