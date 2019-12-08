package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;

/**
 * This is {@link OAuth20ClientCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20ClientCredentialsResponseBuilder extends OAuth20ResourceOwnerCredentialsResponseBuilder {

    public OAuth20ClientCredentialsResponseBuilder(final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                   final OAuth20TokenGenerator accessTokenGenerator,
                                                   final ExpirationPolicyBuilder<OAuth20AccessToken> accessTokenExpirationPolicy,
                                                   final CasConfigurationProperties casProperties) {
        super(accessTokenResponseGenerator, accessTokenGenerator, accessTokenExpirationPolicy, casProperties);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }
}
