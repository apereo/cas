package org.apereo.cas.support.oauth.scopes;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CompositeScopeResolver}.
 *
 * @author sbearcsiro
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class CompositeScopeResolver implements ScopeResolver {

    private final List<ScopeResolver> resolvers;

    @Override
    public boolean supportsService(final AccessTokenRequestContext requestContext) {
        return resolvers.stream().anyMatch(resolver -> resolver.supportsService(requestContext));
    }

    @Override
    public Set<String> resolveRequestScopes(final AccessTokenRequestContext requestContext) {
        return resolvers
                .stream()
                .filter(resolver -> resolver.supportsService(requestContext))
                .map(resolver -> resolver.resolveRequestScopes(requestContext))
                .findFirst()
                .orElseGet(LinkedHashSet::new);
    }

}
