package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.ExpirationPolicy;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.springframework.web.servlet.View;

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
    private final ExpirationPolicy accessTokenExpirationPolicy;
    private final CasConfigurationProperties casProperties;

    @Override
    public View build(final J2EContext context, final String clientId, final AccessTokenRequestDataHolder holder) {
        val accessTokenResult = accessTokenGenerator.generate(holder);
        val result = OAuth20AccessTokenResponseResult.builder()
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenTimeout(accessTokenExpirationPolicy.getTimeToLive())
            .responseType(OAuth20Utils.getResponseType(context))
            .casProperties(casProperties)
            .generatedToken(accessTokenResult)
            .build();
        accessTokenResponseGenerator.generate(context.getRequest(), context.getResponse(), result);
        return null;
    }

    @Override
    public boolean supports(final J2EContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
