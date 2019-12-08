package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.token.JwtBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * This is {@link OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder extends OAuth20TokenAuthorizationResponseBuilder {

    private final IdTokenGeneratorService idTokenGenerator;
    private final ExpirationPolicyBuilder idTokenExpirationPolicy;

    public OidcImplicitIdTokenAndTokenAuthorizationResponseBuilder(final IdTokenGeneratorService idTokenGenerator,
                                                                   final OAuth20TokenGenerator accessTokenGenerator,
                                                                   final ExpirationPolicyBuilder<OAuth20AccessToken> accessTokenExpirationPolicy,
                                                                   final ExpirationPolicyBuilder idTokenExpirationPolicy,
                                                                   final ServicesManager servicesManager,
                                                                   final JwtBuilder accessTokenJwtBuilder,
                                                                   final CasConfigurationProperties casProperties) {
        super(accessTokenGenerator, accessTokenExpirationPolicy, servicesManager, accessTokenJwtBuilder, casProperties);
        this.idTokenGenerator = idTokenGenerator;
        this.idTokenExpirationPolicy = idTokenExpirationPolicy;
    }

    @Override
    protected ModelAndView buildCallbackUrlResponseType(final AccessTokenRequestDataHolder holder,
                                                        final String redirectUri, final OAuth20AccessToken accessToken,
                                                        final List<NameValuePair> params,
                                                        final OAuth20RefreshToken refreshToken,
                                                        final JEEContext context) throws Exception {

        val idToken = this.idTokenGenerator.generate(context.getNativeRequest(),
            context.getNativeResponse(), accessToken, idTokenExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive(),
            OAuth20ResponseTypes.IDTOKEN_TOKEN, holder.getRegisteredService());
        LOGGER.debug("Generated id token [{}]", idToken);
        params.add(new BasicNameValuePair(OidcConstants.ID_TOKEN, idToken));
        return super.buildCallbackUrlResponseType(holder, redirectUri, accessToken, params, refreshToken, context);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.IDTOKEN_TOKEN);
    }
}
