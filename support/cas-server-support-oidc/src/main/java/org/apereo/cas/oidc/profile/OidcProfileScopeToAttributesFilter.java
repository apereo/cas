package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.J2EContext;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    private Collection<BaseOidcScopeAttributeReleasePolicy> userScopes;

    private final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper;
    private final PrincipalFactory principalFactory;
    private final ServicesManager servicesManager;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory,
                                              final ServicesManager servicesManager,
                                              final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes,
                                              final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper) {
        this.attributeToScopeClaimMapper = attributeToScopeClaimMapper;
        this.filters = new HashMap<>();

        final String packageName = BaseOidcScopeAttributeReleasePolicy.class.getPackage().getName();
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().includePackage(packageName))
                        .setUrls(ClasspathHelper.forPackage(packageName))
                        .setScanners(new SubTypesScanner(true)));

        final Set<Class<? extends BaseOidcScopeAttributeReleasePolicy>> subTypes =
                reflections.getSubTypesOf(BaseOidcScopeAttributeReleasePolicy.class);
        subTypes.forEach(Unchecked.consumer(t -> {
            final BaseOidcScopeAttributeReleasePolicy ex = t.newInstance();
            filters.put(ex.getScopeName(), ex);
        }));

        userScopes.forEach(t -> filters.put(t.getScopeName(), t));

        this.principalFactory = principalFactory;
        this.servicesManager = servicesManager;
        this.userScopes = userScopes;
    }

    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService, final J2EContext context) {
        final Principal principal = super.filter(service, profile, registeredService, context);

        if (registeredService instanceof OidcRegisteredService) {
            final OidcRegisteredService oidcService = (OidcRegisteredService) registeredService;
            final Collection<String> scopes = new ArrayList<>(OAuth20Utils.getRequestedScopes(context));
            scopes.addAll(oidcService.getScopes());

            if (!scopes.contains(OidcConstants.OPENID)) {
                LOGGER.debug("Request does not indicate a scope [{}] that can identify OpenID Connect", scopes);
                return principal;
            }

            final Map<String, Object> attributes = new HashMap<>();
            filterAttributesByScope(scopes, attributes, principal, service, oidcService);
            return this.principalFactory.createPrincipal(profile.getId(), attributes);
        }
        return principal;
    }

    private void filterAttributesByScope(final Collection<String> stream,
                                         final Map<String, Object> attributes,
                                         final Principal principal,
                                         final Service service,
                                         final RegisteredService registeredService) {
        stream.stream()
                .distinct()
                .filter(s -> this.filters.containsKey(s))
                .forEach(s -> {
                    final BaseOidcScopeAttributeReleasePolicy policy = filters.get(s);
                    attributes.putAll(policy.getAttributes(principal, service, registeredService));
                });
    }

    @Override
    public void reconcile(final RegisteredService service) {
        if (!(service instanceof OidcRegisteredService)) {
            super.reconcile(service);
            return;
        }

        LOGGER.debug("Reconciling scopes and claims for [{}]", service.getServiceId());

        final List<String> otherScopes = new ArrayList<>();
        final ChainingAttributeReleasePolicy policy = new ChainingAttributeReleasePolicy();
        final OidcRegisteredService oidc = OidcRegisteredService.class.cast(service);

        oidc.getScopes().forEach(s -> {

            LOGGER.debug("Reviewing scope [{}] for [{}]", s, service.getServiceId());

            switch (s.trim().toLowerCase()) {
                case OidcConstants.EMAIL:
                    LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s, OidcEmailScopeAttributeReleasePolicy.class.getSimpleName());
                    policy.getPolicies().add(new OidcEmailScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.ADDRESS:
                    LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                            OidcAddressScopeAttributeReleasePolicy.class.getSimpleName());
                    policy.getPolicies().add(new OidcAddressScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.PROFILE:
                    LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                            OidcProfileScopeAttributeReleasePolicy.class.getSimpleName());
                    policy.getPolicies().add(new OidcProfileScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.PHONE:
                    LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                            OidcProfileScopeAttributeReleasePolicy.class.getSimpleName());
                    policy.getPolicies().add(new OidcPhoneScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.OFFLINE_ACCESS:
                    LOGGER.debug("Given scope [{}], service [{}] is marked to generate refresh tokens", s, service.getId());
                    oidc.setGenerateRefreshToken(true);
                    break;
                case OidcCustomScopeAttributeReleasePolicy.SCOPE_CUSTOM:
                    LOGGER.debug("Found custom scope [{}] for service [{}]", s, service.getId());
                    otherScopes.add(s.trim());
                    break;
                default:
                    LOGGER.debug("[{}] appears to be a user-defined scope and does not match any of the predefined standard scopes. "
                            + "Checking [{}] against user-defined scopes provided as [{}]", s, s, userScopes);

                    final BaseOidcScopeAttributeReleasePolicy userPolicy = userScopes.stream()
                            .filter(t -> t.getScopeName().equals(s.trim()))
                            .findFirst()
                            .orElse(null);
                    if (userPolicy != null) {
                        LOGGER.debug("Mapped user-defined scope [{}] to attribute release policy [{}]", s, userPolicy);
                        policy.getPolicies().add(userPolicy);
                    }
            }
        });
        otherScopes.remove(OidcConstants.OPENID);
        if (!otherScopes.isEmpty()) {
            LOGGER.debug("Mapped scopes [{}] to attribute release policy [{}]", otherScopes,
                    OidcCustomScopeAttributeReleasePolicy.class.getSimpleName());
            policy.getPolicies().add(new OidcCustomScopeAttributeReleasePolicy(otherScopes));
        }

        if (policy.getPolicies().isEmpty()) {
            LOGGER.warn("No attribute release policy could be determined based on given scopes. "
                    + "No claims/attributes will be released to [{}]", service.getId());
            oidc.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        } else {
            oidc.setAttributeReleasePolicy(policy);
        }

        LOGGER.debug("Scope/claim reconciliation for service [{}] resulted in the following attribute release policy [{}]",
                service.getServiceId(), oidc.getAttributeReleasePolicy());

        if (!oidc.equals(service)) {
            LOGGER.debug("Saving scope/claim reconciliation results for service [{}] into registry", service.getServiceId());
            this.servicesManager.save(oidc);
            LOGGER.debug("Saved service [{}] into registry", service.getServiceId());
        } else {
            LOGGER.debug("No changes detected in service [{}] after scope/claim reconciliation", service.getId());
        }
    }
}
