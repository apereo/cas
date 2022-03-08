package org.apereo.cas.support.oauth.scopes;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultOAuth20ScopeResolver}.
 *
 * Since OAuth services don't know about scopes all scopes are allowed.
 *
 * @author sbearcsiro
 * @since 6.6.0
 */
@Order
public class DefaultOAuth20ScopeResolver implements ScopeResolver {

    @Override
    public boolean supportsService(final AccessTokenRequestContext requestContext) {
        return true;
    }

    @Override
    public Set<String> resolveRequestScopes(final AccessTokenRequestContext requestContext) {
        return new LinkedHashSet<>(requestContext.getScopes());
    }

}
