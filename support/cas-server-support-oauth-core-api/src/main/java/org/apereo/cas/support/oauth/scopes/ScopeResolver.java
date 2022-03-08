package org.apereo.cas.support.oauth.scopes;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import java.util.Set;

/**
 * Implementations of this interface resolve the allowed scopes for a given request context.
 *
 * @author sbearcsiro
 * @since 6.6.0
 */
public interface ScopeResolver {

    /**
     * Whether this {@link ScopeResolver} supports the given request context.
     * @param requestContext The request context
     * @return true if the resolver can handle the given context
     */
    boolean supportsService(AccessTokenRequestContext requestContext);

    /**
     * Resolves the scopes for the request context.
     *
     * @param requestContext The request context
     * @return The set of allowed scopes for this request
     */
    Set<String> resolveRequestScopes(AccessTokenRequestContext requestContext);

}
