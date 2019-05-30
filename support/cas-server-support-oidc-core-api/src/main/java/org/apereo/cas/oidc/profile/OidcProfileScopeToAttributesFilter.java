package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.J2EContext;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcProfileScopeToAttributesFilter extends DefaultOAuth20ProfileScopeToAttributesFilter {
    private final Map<String, BaseOidcScopeAttributeReleasePolicy> filters;
    private final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes;

    private final PrincipalFactory principalFactory;
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory,
                                              final ServicesManager servicesManager,
                                              final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes,
                                              final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
        this.filters = new LinkedHashMap<>();
        this.principalFactory = principalFactory;
        this.servicesManager = servicesManager;
        this.userScopes = userScopes;

        configureAttributeReleasePoliciesByScope();
    }

    private void configureAttributeReleasePoliciesByScope() {
        val oidc = casProperties.getAuthn().getOidc();
        val packageName = BaseOidcScopeAttributeReleasePolicy.class.getPackage().getName();
        val reflections =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().includePackage(packageName))
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new SubTypesScanner(true)));

        val subTypes = reflections.getSubTypesOf(BaseOidcScopeAttributeReleasePolicy.class);
        subTypes.forEach(Unchecked.consumer(t -> {
            if (ClassUtils.hasConstructor(t)) {
                val ex = t.getDeclaredConstructor().newInstance();
                if (oidc.getScopes().contains(ex.getScopeType())) {
                    LOGGER.trace("Found standard OpenID Connect scope [{}] to filter attributes", ex.getScopeType());
                    filters.put(ex.getScopeType(), ex);
                } else {
                    LOGGER.debug("OpenID Connect scope [{}] is not configured for use and will be ignored", ex.getScopeType());
                }
            }
        }));

        if (!userScopes.isEmpty()) {
            LOGGER.debug("Configuring attributes release policies for user-defined scopes [{}]", userScopes);
            userScopes.forEach(t -> filters.put(t.getScopeType(), t));
        }
    }

    @Override
    public Principal filter(final Service service,
                            final Principal profile,
                            final RegisteredService registeredService,
                            final J2EContext context,
                            final AccessToken accessToken) {
        val principal = super.filter(service, profile, registeredService, context, accessToken);

        if (registeredService instanceof OidcRegisteredService) {
            val scopes = new LinkedHashSet<String>(accessToken.getScopes());
            if (!scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
                LOGGER.warn("Request does not indicate a scope [{}] that can identify an OpenID Connect request. "
                    + "This is a REQUIRED scope that MUST be present in the request. Given its absence, "
                    + "CAS will not process any attribute claims and will return the authenticated principal as is.", scopes);
                return principal;
            }

            val oidcService = (OidcRegisteredService) registeredService;
            scopes.retainAll(oidcService.getScopes());

            val attributes = filterAttributesByScope(scopes, principal, service, oidcService, accessToken);
            LOGGER.debug("Collection of attributes filtered by scopes [{}] are [{}]", scopes, attributes);

            filterAttributesByAccessTokenRequestedClaims(oidcService, accessToken, principal, attributes);
            LOGGER.debug("Final collection of attributes are [{}]", attributes);
            return this.principalFactory.createPrincipal(profile.getId(), attributes);
        }
        return principal;
    }

    /**
     * Filter attributes by access token requested claims.
     *
     * @param oidcService the oidc service
     * @param accessToken the access token
     * @param principal   the principal
     * @param attributes  the attributes
     */
    protected void filterAttributesByAccessTokenRequestedClaims(final OidcRegisteredService oidcService,
                                                                final AccessToken accessToken,
                                                                final Principal principal,
                                                                final Map<String, List<Object>> attributes) {
        val userinfo = OAuth20Utils.parseUserInfoRequestClaims(accessToken);
        val principalAttributes = accessToken.getTicketGrantingTicket().getAuthentication().getPrincipal().getAttributes();
        LOGGER.debug("Requested user-info claims [{}] are compared against principal attributes [{}]", userinfo, principalAttributes);
        userinfo
            .stream()
            .filter(principalAttributes::containsKey)
            .forEach(key -> attributes.put(key, principalAttributes.get(key)));
    }

    /**
     * Filter attributes by scope map.
     *
     * @param scopes            the scopes
     * @param principal         the principal
     * @param service           the service
     * @param registeredService the registered service
     * @param accessToken       the access token
     * @return the map
     */
    protected Map<String, List<Object>> filterAttributesByScope(final Collection<String> scopes,
                                                                final Principal principal,
                                                                final Service service,
                                                                final RegisteredService registeredService,
                                                                final AccessToken accessToken) {
        if (scopes.isEmpty()) {
            val attributes = principal.getAttributes();
            LOGGER.trace("No defined scopes are available to instruct attribute release policies for [{}]. "
                    + "CAS will authorize the collection of resolved attributes [{}] for release to [{}]",
                registeredService.getServiceId(), attributes, service.getId());
            return attributes;
        }

        val attributes = new LinkedHashMap<String, List<Object>>();
        scopes
            .stream()
            .distinct()
            .filter(this.filters::containsKey)
            .forEach(s -> {
                val policy = filters.get(s);
                attributes.putAll(policy.getAttributes(principal, service, registeredService));
            });
        return attributes;
    }

    @SneakyThrows
    @Override
    public RegisteredService reconcile(final RegisteredService service) {
        if (!(service instanceof OidcRegisteredService)) {
            return super.reconcile(service);
        }
        LOGGER.trace("Reconciling OpenId Connect scopes and claims for [{}]", service.getServiceId());

        val policyChain = new ChainingAttributeReleasePolicy();

        val oidcService = (OidcRegisteredService) BeanUtils.cloneBean(service);

        val definedServiceScopes = oidcService.getScopes();
        definedServiceScopes.forEach(givenScope -> {
            LOGGER.trace("Reviewing scope [{}] for [{}]", givenScope, service.getServiceId());
            try {
                val scope = OidcConstants.StandardScopes.valueOf(givenScope.trim().toUpperCase());
                switch (scope) {
                    case EMAIL:
                        addAttributeReleasePolicy(policyChain, new OidcEmailScopeAttributeReleasePolicy(), givenScope, oidcService);
                        break;
                    case ADDRESS:
                        addAttributeReleasePolicy(policyChain, new OidcAddressScopeAttributeReleasePolicy(), givenScope, oidcService);
                        break;
                    case PROFILE:
                        addAttributeReleasePolicy(policyChain, new OidcProfileScopeAttributeReleasePolicy(), givenScope, oidcService);
                        break;
                    case PHONE:
                        addAttributeReleasePolicy(policyChain, new OidcPhoneScopeAttributeReleasePolicy(), givenScope, oidcService);
                        break;
                    case OFFLINE_ACCESS:
                        LOGGER.debug("Given scope [{}], service [{}] is marked to generate refresh tokens", givenScope, service.getId());
                        oidcService.setGenerateRefreshToken(true);
                        break;
                    default:
                        LOGGER.debug("Scope [{}] is unsupported for service [{}]", givenScope, service.getId());
                        break;
                }
            } catch (final Exception e) {
                LOGGER.debug("[{}] appears to be a user-defined scope and does not match any of the predefined standard scopes. "
                    + "Checking [{}] against user-defined scopes provided as [{}]", givenScope, givenScope, userScopes);

                userScopes
                    .stream()
                    .filter(obj -> obj instanceof OidcCustomScopeAttributeReleasePolicy)
                    .map(t -> (OidcCustomScopeAttributeReleasePolicy) t)
                    .filter(t -> t.getScopeName().equals(givenScope.trim()))
                    .findFirst()
                    .ifPresent(userPolicy -> addAttributeReleasePolicy(policyChain, userPolicy, givenScope, oidcService));

            }
        });

        if (definedServiceScopes.isEmpty()) {
            LOGGER.trace("Registered service [{}] does not define any scopes to control attribute release policies. "
                + "CAS will allow the existing attribute release policies assigned to the service to operate without a scope.", service.getServiceId());
        } else if (policyChain.getPolicies().isEmpty()) {
            LOGGER.debug("No attribute release policy could be determined based on given scopes. "
                + "No claims/attributes will be released to [{}]", service.getServiceId());
            oidcService.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        } else {
            oidcService.setAttributeReleasePolicy(policyChain);
        }

        LOGGER.trace("Scope/claim reconciliation for service [{}] resulted in the following attribute release policy [{}]",
            service.getServiceId(), oidcService.getAttributeReleasePolicy());

        if (!oidcService.equals(service)) {
            LOGGER.trace("Saving scope/claim reconciliation results for service [{}] into registry", service.getServiceId());
            this.servicesManager.save(oidcService);
            LOGGER.debug("Saved service [{}] into registry", service.getServiceId());
            return oidcService;
        }
        LOGGER.trace("No changes detected in service [{}] after scope/claim reconciliation", service.getId());
        return service;
    }

    private static void addAttributeReleasePolicy(final ChainingAttributeReleasePolicy chain,
                                           final BaseOidcScopeAttributeReleasePolicy policyToAdd,
                                           final String givenScope,
                                           final OidcRegisteredService registeredService) {
        LOGGER.debug("Mapped [{}] to attribute release policy [{}]", givenScope, policyToAdd.getClass().getSimpleName());
        policyToAdd.setConsentPolicy(registeredService.getAttributeReleasePolicy().getConsentPolicy());
        chain.getPolicies().add(policyToAdd);
    }
}
