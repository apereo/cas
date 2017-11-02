package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
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

    private final Map<String, BaseOidcScopeAttributeReleasePolicy> filters;
    private final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes;

    private final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper;
    private final PrincipalFactory principalFactory;
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory,
                                              final ServicesManager servicesManager,
                                              final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes,
                                              final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper,
                                              final CasConfigurationProperties casProperties) {
        this.attributeToScopeClaimMapper = attributeToScopeClaimMapper;
        this.casProperties = casProperties;
        this.filters = new HashMap<>();
        this.principalFactory = principalFactory;
        this.servicesManager = servicesManager;
        this.userScopes = userScopes;

        configureAttributeReleasePoliciesByScope();
    }

    private void configureAttributeReleasePoliciesByScope() {
        final OidcProperties oidc = casProperties.getAuthn().getOidc();
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
            if (oidc.getScopes().contains(ex.getScopeName())) {
                LOGGER.debug("Found OpenID Connect scope [{}] to filter attributes", ex.getScopeName());
                filters.put(ex.getScopeName(), ex);
            } else {
                LOGGER.debug("OpenID Connect scope [{}] is not configured for use and will be ignored", ex.getScopeName());
            }
        }));

        if (!userScopes.isEmpty()) {
            LOGGER.debug("Configuring attributes release policies for user-defined scopes [{}]", userScopes);
            userScopes.forEach(t -> filters.put(t.getScopeName(), t));
        }
    }

    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService,
                            final J2EContext context) {
        final Principal principal = super.filter(service, profile, registeredService, context);

        if (registeredService instanceof OidcRegisteredService) {
            final OidcRegisteredService oidcService = (OidcRegisteredService) registeredService;
            final Collection<String> scopes = new ArrayList<>(OAuth20Utils.getRequestedScopes(context));
            scopes.addAll(oidcService.getScopes());

            if (!scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
                LOGGER.debug("Request does not indicate a scope [{}] that can identify an OpenID Connect request. "
                        + "This is a REQUIRED scope that MUST be present in the request. Given its absence, "
                        + "CAS will not process any attribute claims and will return the authenticated principal as is.", scopes);
                return principal;
            }

            final Map<String, Object> attributes = filterAttributesByScope(scopes, principal, service, oidcService);
            return this.principalFactory.createPrincipal(profile.getId(), attributes);
        }
        return principal;
    }

    private Map<String, Object> filterAttributesByScope(final Collection<String> stream,
                                                        final Principal principal,
                                                        final Service service,
                                                        final RegisteredService registeredService) {
        final OidcProperties oidc = casProperties.getAuthn().getOidc();
        final Map<String, Object> attributes = new HashMap<>();
        stream.stream()
                .distinct()
                .filter(this.filters::containsKey)
                .forEach(s -> {
                    final BaseOidcScopeAttributeReleasePolicy policy = filters.get(s);
                    policy.setSupportedClaims(oidc.getClaims());
                    attributes.putAll(policy.getAttributes(principal, service, registeredService));
                });
        return attributes;
    }

    @Override
    public void reconcile(final RegisteredService service) {
        if (!(service instanceof OidcRegisteredService)) {
            super.reconcile(service);
            return;
        }

        LOGGER.debug("Reconciling OpenId Connect scopes and claims for [{}]", service.getServiceId());

        final List<String> otherScopes = new ArrayList<>();
        final ChainingAttributeReleasePolicy policy = new ChainingAttributeReleasePolicy();
        final OidcRegisteredService oidc = OidcRegisteredService.class.cast(service);

        oidc.getScopes().forEach(s -> {
            LOGGER.debug("Reviewing scope [{}] for [{}]", s, service.getServiceId());

            try {
                final OidcConstants.StandardScopes scope = OidcConstants.StandardScopes.valueOf(s.trim().toLowerCase().toUpperCase());
                switch (scope) {
                    case EMAIL:
                        LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s, OidcEmailScopeAttributeReleasePolicy.class.getSimpleName());
                        policy.getPolicies().add(new OidcEmailScopeAttributeReleasePolicy());
                        break;
                    case ADDRESS:
                        LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                                OidcAddressScopeAttributeReleasePolicy.class.getSimpleName());
                        policy.getPolicies().add(new OidcAddressScopeAttributeReleasePolicy());
                        break;
                    case PROFILE:
                        LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                                OidcProfileScopeAttributeReleasePolicy.class.getSimpleName());
                        policy.getPolicies().add(new OidcProfileScopeAttributeReleasePolicy());
                        break;
                    case PHONE:
                        LOGGER.debug("Mapped [{}] to attribute release policy [{}]", s,
                                OidcProfileScopeAttributeReleasePolicy.class.getSimpleName());
                        policy.getPolicies().add(new OidcPhoneScopeAttributeReleasePolicy());
                        break;
                    case OFFLINE_ACCESS:
                        LOGGER.debug("Given scope [{}], service [{}] is marked to generate refresh tokens", s, service.getId());
                        oidc.setGenerateRefreshToken(Boolean.TRUE);
                        break;
                    case CUSTOM:
                        LOGGER.debug("Found custom scope [{}] for service [{}]", s, service.getId());
                        otherScopes.add(s.trim());
                        break;
                    default:
                        LOGGER.debug("Scope [{}] is unsupported for service [{}]", s, service.getId());
                        break;
                }
            } catch (final Exception e) {
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
        otherScopes.remove(OidcConstants.StandardScopes.OPENID.getScope());
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
