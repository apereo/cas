package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;

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
    public boolean supports(final WebContext context) {
        val grantType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.GRANT_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }
}
