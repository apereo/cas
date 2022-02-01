package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20ResourceOwnerCredentialsResponseBuilder<T extends OAuth20ConfigurationContext>
    extends BaseOAuth20AuthorizationResponseBuilder<T> {

    public OAuth20ResourceOwnerCredentialsResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public ModelAndView build(final String clientId,
                              final AccessTokenRequestDataHolder holder) throws Exception {
        val accessTokenResult = configurationContext.getAccessTokenGenerator().generate(holder);
        val result = OAuth20AccessTokenResponseResult.builder()
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenTimeout(accessTokenResult.getAccessToken().map(OAuth20AccessToken::getExpiresIn).orElse(0L))
            .responseType(holder.getResponseType())
            .casProperties(configurationContext.getCasProperties())
            .generatedToken(accessTokenResult)
            .grantType(holder.getGrantType())
            .build();
        configurationContext.getAccessTokenResponseGenerator().generate(result);
        return new ModelAndView();
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
