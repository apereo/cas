package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
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
public class OAuth20ResourceOwnerCredentialsResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder {
    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final OAuth20TokenGenerator accessTokenGenerator;

    public OAuth20ResourceOwnerCredentialsResponseBuilder(final ServicesManager servicesManager,
                                                          final CasConfigurationProperties casProperties,
                                                          final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                          final OAuth20TokenGenerator accessTokenGenerator,
                                                          final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(servicesManager, casProperties, authorizationModelAndViewBuilder);
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public ModelAndView build(final WebContext context, final String clientId,
                              final AccessTokenRequestDataHolder holder) {
        val accessTokenResult = accessTokenGenerator.generate(holder);
        val result = OAuth20AccessTokenResponseResult.builder()
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenTimeout(accessTokenResult.getAccessToken().map(OAuth20AccessToken::getExpiresIn).orElse(0L))
            .responseType(OAuth20Utils.getResponseType(context))
            .casProperties(casProperties)
            .generatedToken(accessTokenResult)
            .grantType(holder.getGrantType())
            .build();
        accessTokenResponseGenerator.generate(context, result);
        return new ModelAndView();
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
