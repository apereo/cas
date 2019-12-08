package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class OAuth20ResourceOwnerCredentialsResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final ExpirationPolicyBuilder<OAuth20AccessToken> accessTokenExpirationPolicy;
    private final CasConfigurationProperties casProperties;

    @Override
    public ModelAndView build(final JEEContext context, final String clientId,
                              final AccessTokenRequestDataHolder holder) {
        val accessTokenResult = accessTokenGenerator.generate(holder);
        val expirationPolicy = accessTokenExpirationPolicy.buildTicketExpirationPolicy();
        val result = OAuth20AccessTokenResponseResult.builder()
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenTimeout(expirationPolicy.getTimeToLive())
            .responseType(OAuth20Utils.getResponseType(context))
            .casProperties(casProperties)
            .generatedToken(accessTokenResult)
            .build();
        accessTokenResponseGenerator.generate(context.getNativeRequest(), context.getNativeResponse(), result);
        return new ModelAndView();
    }

    @Override
    public boolean supports(final JEEContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
