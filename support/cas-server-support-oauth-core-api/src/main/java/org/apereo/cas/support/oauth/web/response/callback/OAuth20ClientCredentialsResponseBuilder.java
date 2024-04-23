package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;

/**
 * This is {@link OAuth20ClientCredentialsResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20ClientCredentialsResponseBuilder<T extends OAuth20ConfigurationContext> extends OAuth20ResourceOwnerCredentialsResponseBuilder<T> {

    public OAuth20ClientCredentialsResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return OAuth20Utils.isGrantType(context.getGrantType(), OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }
}
