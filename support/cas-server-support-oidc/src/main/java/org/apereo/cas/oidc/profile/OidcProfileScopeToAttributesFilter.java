package org.apereo.cas.oidc.profile;

import org.apache.shiro.util.ClassUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.pac4j.core.context.J2EContext;
import org.reflections.Reflections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OidcProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcProfileScopeToAttributesFilter extends DefaultOAuth20ProfileScopeToAttributesFilter {
    private Map<String, BaseOidcScopeAttributeReleasePolicy> filters;
    private final PrincipalFactory principalFactory;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory) {
        filters = new HashMap<>();

        final Reflections reflections = new Reflections(getClass().getPackage().getName());
        final Set<Class<? extends BaseOidcScopeAttributeReleasePolicy>> subTypes =
                reflections.getSubTypesOf(BaseOidcScopeAttributeReleasePolicy.class);
        subTypes.forEach(t -> {
            final BaseOidcScopeAttributeReleasePolicy ex = (BaseOidcScopeAttributeReleasePolicy) ClassUtils.newInstance(t);
            filters.put(ex.getScopeName(), ex);
        });
        this.principalFactory = principalFactory;
    }

    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService, final J2EContext context) {
        final Principal principal = super.filter(service, profile, registeredService, context);
        final Collection<String> scopes = getRequestedScopes(context);
        final Map<String, Object> attributes = new HashMap<>();

        scopes.stream()
                .distinct()
                .filter(s -> this.filters.containsKey(s))
                .forEach(s -> {
                    final BaseOidcScopeAttributeReleasePolicy policy = filters.get(s);
                    attributes.putAll(policy.getAttributes(principal, registeredService));
                });

        return this.principalFactory.createPrincipal(profile.getId(), attributes);
    }
}
