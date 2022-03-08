package org.apereo.cas.oidc.scopes;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.scopes.ScopeResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcScopeResolver}.
 *
 * @author sbearcsiro
 * @since 6.6.0
 */
@Order(0)
@RequiredArgsConstructor
public class OidcScopeResolver implements ScopeResolver {

    private final CasConfigurationProperties casProperties;

    @Override
    public boolean supportsService(final AccessTokenRequestContext requestContext) {
        return requestContext.getRegisteredService() instanceof OidcRegisteredService;
    }

    @Override
    public Set<String> resolveRequestScopes(final AccessTokenRequestContext requestContext) {
        return Optional.of(requestContext.getRegisteredService())
                .filter(OidcRegisteredService.class::isInstance)
                .map(OidcRegisteredService.class::cast)
                .map(service -> {
                    val scopes = new LinkedHashSet<>(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
                    val serviceScopes = service.getScopes();
                    if (!serviceScopes.isEmpty()) {
                        scopes.retainAll(serviceScopes);
                    }
                    scopes.retainAll(requestContext.getScopes());
                    scopes.add(OidcConstants.StandardScopes.OPENID.getScope());
                    return scopes;
                })
                .orElseGet(LinkedHashSet::new);
    }

}
