package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
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
    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    public ModelAndView build(final AccessTokenRequestContext holder) throws Throwable {
        val accessTokenResult = configurationContext.getAccessTokenGenerator().generate(holder);
        val accessTokenTimeout = determineAccessTokenTimeoutInSeconds(accessTokenResult);
        val result = OAuth20AccessTokenResponseResult.builder()
            .registeredService(holder.getRegisteredService())
            .service(holder.getService())
            .accessTokenTimeout(accessTokenTimeout)
            .responseType(holder.getResponseType())
            .grantType(holder.getGrantType())
            .casProperties(configurationContext.getCasProperties())
            .generatedToken(accessTokenResult)
            .userProfile(holder.getUserProfile())
            .build();
        configurationContext.getAccessTokenResponseGenerator().generate(result);
        return new ModelAndView();
    }

    protected Long determineAccessTokenTimeoutInSeconds(final OAuth20TokenGeneratedResult accessTokenResult) {
        return OAuth20Utils.getAccessTokenTimeout(accessTokenResult);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return OAuth20Utils.isGrantType(context.getGrantType(), OAuth20GrantTypes.PASSWORD);
    }
}
