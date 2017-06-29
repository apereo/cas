package org.apereo.cas.support.oauth.web.response.callback;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.pac4j.core.context.J2EContext;
import org.springframework.web.servlet.View;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20ResourceOwnerCredentialsResponseBuilder implements OAuth20AuthorizationResponseBuilder {
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final ExpirationPolicy accessTokenExpirationPolicy;

    public OAuth20ResourceOwnerCredentialsResponseBuilder(final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                          final OAuth20TokenGenerator accessTokenGenerator,
                                                          final ExpirationPolicy accessTokenExpirationPolicy) {
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenExpirationPolicy = accessTokenExpirationPolicy;
    }

    @Override
    public View build(final J2EContext context, final String clientId, final AccessTokenRequestDataHolder holder) {
        final Pair<AccessToken, RefreshToken> accessToken = accessTokenGenerator.generate(holder);
        accessTokenResponseGenerator.generate(context.getRequest(),
                context.getResponse(),
                holder.getRegisteredService(), holder.getService(),
                accessToken.getKey(), accessToken.getValue(),
                accessTokenExpirationPolicy.getTimeToLive(),
                OAuth20Utils.getResponseType(context));
        return null;
    }

    @Override
    public boolean supports(final J2EContext context) {
        final String grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
