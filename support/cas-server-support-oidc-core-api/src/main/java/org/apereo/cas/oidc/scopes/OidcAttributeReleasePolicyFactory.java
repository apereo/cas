package org.apereo.cas.oidc.scopes;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcAttributeReleasePolicyFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface OidcAttributeReleasePolicyFactory {
    /**
     * Get attribute release policy.
     *
     * @param scope the scope
     * @return the release policy
     */
    BaseOidcScopeAttributeReleasePolicy get(OidcConstants.StandardScopes scope);

    /**
     * Custom.
     *
     * @param name              the name
     * @param allowedAttributes the allowed attributes
     * @return the base oidc scope attribute release policy
     */
    OidcCustomScopeAttributeReleasePolicy custom(String name, List<String> allowedAttributes);

    /**
     * Custom.
     *
     * @param name              the name
     * @param allowedAttributes the allowed attributes
     * @return the base oidc scope attribute release policy
     */
    default OidcCustomScopeAttributeReleasePolicy custom(final String name, final String allowedAttributes) {
        return custom(name, Arrays.stream(allowedAttributes.split(",")).collect(Collectors.toList()));
    }

    /**
     * From map of user-defined scopes.
     *
     * @param userDefinedScopes the user defined scopes
     * @return the collection
     */
    default Set<OidcCustomScopeAttributeReleasePolicy> from(final Map<String, String> userDefinedScopes) {
        return userDefinedScopes.entrySet()
            .stream()
            .map(k -> custom(k.getKey(), k.getValue()))
            .collect(Collectors.toSet());
    }

    /**
     * Gets user defined scopes.
     *
     * @return the user defined scopes
     */
    Collection<OidcCustomScopeAttributeReleasePolicy> getUserDefinedScopes();
}
