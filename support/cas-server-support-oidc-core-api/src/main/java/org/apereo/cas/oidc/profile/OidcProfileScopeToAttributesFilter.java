package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OidcProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcProfileScopeToAttributesFilter extends DefaultOAuth20ProfileScopeToAttributesFilter {

    private final PrincipalFactory principalFactory;

    private final CasConfigurationProperties casProperties;

    private final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public Principal filter(final Service service,
                            final Principal givenPrincipal,
                            final RegisteredService registeredService,
                            final Set<String> scopes,
                            final OAuth20AccessToken accessToken) throws Throwable {
        val principal = super.filter(service, givenPrincipal, registeredService, scopes, accessToken);
        if (registeredService instanceof final OidcRegisteredService oidcService) {
            return filterClaimsForOidcService(service, givenPrincipal, accessToken, principal, scopes, oidcService);
        }
        return principal;
    }

    protected Principal filterClaimsForOidcService(final Service service,
                                                   final Principal givenPrincipal,
                                                   final OAuth20AccessToken accessToken,
                                                   final Principal filteredPrincipal,
                                                   final Set<String> scopes,
                                                   final OidcRegisteredService oidcService) throws Throwable {
        if (!scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Access token scopes [{}] cannot identify an OpenID Connect request with [{}] scope(s). "
                    + "This is a REQUIRED scope for OpenID Connect that MUST be present in the request. Given its absence, "
                    + "CAS will not process any attribute claims and will return the authenticated principal as is.",
                OidcConstants.StandardScopes.OPENID.getScope(), scopes.isEmpty() ? "empty" : scopes);
            return principalFactory.createPrincipal(givenPrincipal.getId());
        }

        scopes.retainAll(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
        LOGGER.debug("Collection of scopes filtered based on discovery settings are [{}]", scopes);

        val attributes = getAttributesAllowedForService(scopes, filteredPrincipal, service, oidcService, accessToken);
        LOGGER.debug("Collection of claims filtered by scopes [{}] are [{}]", scopes, attributes);

        filterAttributesByAccessTokenRequestedClaims(oidcService, accessToken, filteredPrincipal, attributes);
        LOGGER.debug("Final collection of claims are [{}]", attributes);
        return principalFactory.createPrincipal(givenPrincipal.getId(), attributes);
    }

    protected void filterAttributesByAccessTokenRequestedClaims(final OidcRegisteredService oidcService,
                                                                final OAuth20AccessToken accessToken,
                                                                final Principal principal,
                                                                final Map<String, List<Object>> attributes) {
        val userInfo = OAuth20Utils.parseUserInfoRequestClaims(accessToken);
        if (userInfo.isEmpty()) {
            LOGGER.trace("No userinfo requested claims are available");
        } else if (accessToken != null && accessToken.getTicketGrantingTicket() instanceof final AuthenticationAwareTicket aat) {
            val principalAttributes = aat.getAuthentication().getPrincipal().getAttributes();
            LOGGER.debug("Requested user-info claims [{}] are compared against principal attributes [{}]", userInfo, principalAttributes);
            userInfo
                .stream()
                .filter(principalAttributes::containsKey)
                .forEach(key -> attributes.put(key, principalAttributes.get(key)));
        }
    }

    protected Map<String, List<Object>> filterAttributesByScope(final Collection<String> scopes,
                                                                final Principal principal,
                                                                final Service service,
                                                                final OidcRegisteredService registeredService,
                                                                final OAuth20AccessToken accessToken) {
        if (scopes.isEmpty()) {
            LOGGER.info("No defined scopes are available to instruct attribute release policies for [{}]. "
                    + "CAS will NOT authorize the collection of resolved claims for release to [{}]",
                registeredService.getServiceId(), service.getId());
            return new HashMap<>();
        }

        val effectiveAttributeReleasePolicies = oidcAttributeReleasePolicyFactory.resolvePolicies(registeredService);
        val attributes = new LinkedHashMap<String, List<Object>>();
        scopes
            .stream()
            .distinct()
            .filter(effectiveAttributeReleasePolicies::containsKey)
            .map(scope -> {
                val policy = effectiveAttributeReleasePolicies.get(scope);
                return getAttributesFromPolicy(principal, service, registeredService, policy);
            })
            .forEach(attributes::putAll);

        effectiveAttributeReleasePolicies.values()
            .stream()
            .filter(OidcScopeFreeAttributeReleasePolicy.class::isInstance)
            .forEach(policy -> {
                val policyAttr = getAttributesFromPolicy(principal, service, registeredService, policy);
                attributes.putAll(policyAttr);
            });
        LOGGER.debug("Final collection of attributes based on scopes are [{}]", attributes);
        return attributes;
    }

    protected Map<String, List<Object>> getAttributesFromPolicy(final Principal principal,
                                                                final Service service,
                                                                final OidcRegisteredService registeredService,
                                                                final BaseOidcScopeAttributeReleasePolicy policy) {
        return FunctionUtils.doUnchecked(() -> {
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(service)
                .principal(principal)
                .applicationContext(applicationContext)
                .build();
            val policyAttr = policy.getAttributes(releasePolicyContext);
            LOGGER.debug("Calculated attributes [{}] via attribute release policy [{}]", policyAttr, policy.getName());
            return policyAttr;
        });
    }

    protected Map<String, List<Object>> getAttributesAllowedForService(
        final Collection<String> scopes,
        final Principal principal,
        final Service service,
        final OidcRegisteredService oidcService,
        final OAuth20AccessToken accessToken) throws Throwable {
        val serviceScopes = oidcService.getScopes();
        LOGGER.trace("Scopes assigned to service definition [{}] are [{}]", oidcService.getName(), serviceScopes);
        val scopeFree = serviceScopes.isEmpty() || (serviceScopes.size() == 1
            && serviceScopes.contains(OidcConstants.StandardScopes.OPENID.getScope()));
        if (!scopeFree) {
            scopes.retainAll(serviceScopes);
            LOGGER.trace("Service definition [{}] will filter claims based on scopes [{}]", oidcService.getName(), scopes);
            return filterAttributesByScope(scopes, principal, service, oidcService, accessToken);
        }
        LOGGER.trace("Service definition [{}] invokes the assigned claims release policy without using scopes", oidcService.getName());
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(oidcService)
            .service(service)
            .principal(principal)
            .applicationContext(applicationContext)
            .attributeReleasePolicyPredicate(policy ->
                !(policy instanceof final OidcRegisteredServiceAttributeReleasePolicy oidcPolicy)
                    || scopes.contains(oidcPolicy.getScopeType()))
            .build();
        return oidcService.getAttributeReleasePolicy().getAttributes(releasePolicyContext);
    }
}
