package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20ClientCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20ClientCredentialsResponseBuilder extends OAuth20ResourceOwnerCredentialsResponseBuilder {

    public OAuth20ClientCredentialsResponseBuilder(final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                   final OAuth20TokenGenerator accessTokenGenerator,
                                                   final ExpirationPolicy accessTokenExpirationPolicy) {
        super(accessTokenResponseGenerator, accessTokenGenerator, accessTokenExpirationPolicy);
    }


    @Override
    public boolean supports(final J2EContext context) {
        final String grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }
}
