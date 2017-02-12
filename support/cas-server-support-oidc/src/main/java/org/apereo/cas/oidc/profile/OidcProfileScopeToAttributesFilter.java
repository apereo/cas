package org.apereo.cas.oidc.profile;

import org.apache.shiro.util.ClassUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.pac4j.core.context.J2EContext;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcProfileScopeToAttributesFilter.class);

    private Map<String, BaseOidcScopeAttributeReleasePolicy> filters;
    private final PrincipalFactory principalFactory;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory) {
        filters = new HashMap<>();

        final String packageName = BaseOidcScopeAttributeReleasePolicy.class.getPackage().getName();
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().includePackage(packageName))
                        .setUrls(ClasspathHelper.forPackage(packageName))
                        .setScanners(new SubTypesScanner(true)));

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
        final Collection<String> scopes = OAuthUtils.getRequestedScopes(context);
        if (scopes.isEmpty() || !scopes.contains(OidcConstants.OPENID)) {
            LOGGER.debug("Request does not indicate a scope [{}] that can identify OpenID Connect", scopes);
            return principal;
        }

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
