package org.apereo.cas.oidc.web;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import java.util.List;

/**
 * This is {@link OidcImplicitIdTokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcImplicitIdTokenAuthorizationResponseBuilder extends OAuth20TokenAuthorizationResponseBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcImplicitIdTokenAuthorizationResponseBuilder.class);
    private final OidcIdTokenGeneratorService idTokenGenerator;
    private final ExpirationPolicy idTokenExpirationPolicy;

    public OidcImplicitIdTokenAuthorizationResponseBuilder(final OidcIdTokenGeneratorService idTokenGenerator,
                                                           final OAuth20TokenGenerator accessTokenGenerator,
                                                           final ExpirationPolicy accessTokenExpirationPolicy,
                                                           final ExpirationPolicy idTokenExpirationPolicy) {
        super(accessTokenGenerator, accessTokenExpirationPolicy);
        this.idTokenGenerator = idTokenGenerator;
        this.idTokenExpirationPolicy = idTokenExpirationPolicy;
    }

    @Override
    protected View buildCallbackUrlResponseType(final AccessTokenRequestDataHolder holder,
                                                final String redirectUri, final AccessToken accessToken,
                                                final List<NameValuePair> params,
                                                final RefreshToken refreshToken,
                                                final J2EContext context) throws Exception {

        final String idToken = this.idTokenGenerator.generate(context.getRequest(),
                context.getResponse(), accessToken, idTokenExpirationPolicy.getTimeToLive(),
                OAuth20ResponseTypes.IDTOKEN_TOKEN, holder.getRegisteredService());
        LOGGER.debug("Generated id token [{}]", idToken);
        params.add(new BasicNameValuePair(OidcConstants.ID_TOKEN, idToken));
        return super.buildCallbackUrlResponseType(holder, redirectUri, accessToken, params, refreshToken, context);
    }

    @Override
    public boolean supports(final J2EContext context) {
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.IDTOKEN_TOKEN);
    }
}
