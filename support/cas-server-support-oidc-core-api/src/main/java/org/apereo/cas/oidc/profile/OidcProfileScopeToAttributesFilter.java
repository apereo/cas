package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Collection;
import java.util.HashMap;
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
@RequiredArgsConstructor
public class OidcProfileScopeToAttributesFilter extends DefaultOAuth20ProfileScopeToAttributesFilter {

    private final PrincipalFactory principalFactory;

    private final CasConfigurationProperties casProperties;

    private final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public Principal filter(final Service service, final Principal profile,
                            final RegisteredService registeredService,
                            final OAuth20AccessToken accessToken) throws Throwable {
        val principal = super.filter(service, profile, registeredService, accessToken);
        if (registeredService instanceof final OidcRegisteredService oidcService) {
            return filterClaimsForOidcService(service, profile, accessToken, principal, oidcService);
        }
        return principal;
    }

    protected Principal filterClaimsForOidcService(final Service service, final Principal profile,
                                                   final OAuth20AccessToken accessToken,
                                                   final Principal principal,
                                                   final OidcRegisteredService oidcService) throws Throwable {
        val scopes = new LinkedHashSet<>(accessToken.getScopes());
        if (!scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Access token scopes [{}] cannot identify an OpenID Connect request with a [{}] scope. "
                    + "This is a REQUIRED scope that MUST be present in the request. Given its absence, "
                    + "CAS will not process any attribute claims and will return the authenticated principal as is.",
                OidcConstants.StandardScopes.OPENID.getScope(), scopes);
            return principalFactory.createPrincipal(profile.getId());
        }

        scopes.retainAll(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
        LOGGER.debug("Collection of scopes filtered based on discovery settings are [{}]", scopes);

        val attributes = getAttributesAllowedForService(scopes, principal, service, oidcService, accessToken);
        LOGGER.debug("Collection of claims filtered by scopes [{}] are [{}]", scopes, attributes);

        filterAttributesByAccessTokenRequestedClaims(oidcService, accessToken, principal, attributes);
        LOGGER.debug("Final collection of claims are [{}]", attributes);
        return principalFactory.createPrincipal(profile.getId(), attributes);
    }

    protected void filterAttributesByAccessTokenRequestedClaims(final OidcRegisteredService oidcService,
                                                                final OAuth20AccessToken accessToken,
                                                                final Principal principal,
                                                                final Map<String, List<Object>> attributes) {
        val userInfo = OAuth20Utils.parseUserInfoRequestClaims(accessToken);
        if (userInfo.isEmpty()) {
            LOGGER.trace("No userinfo requested claims are available");
        } else {
            val principalAttributes = accessToken.getTicketGrantingTicket().getAuthentication().getPrincipal().getAttributes();
            LOGGER.debug("Requested user-info claims [{}] are compared against principal attributes [{}]",
                userInfo, principalAttributes);
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
            .map(Unchecked.function(scope -> {
                val policy = effectiveAttributeReleasePolicies.get(scope);
                val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                    .registeredService(registeredService)
                    .service(service)
                    .principal(principal)
                    .applicationContext(applicationContext)
                    .build();
                val policyAttr = policy.getAttributes(releasePolicyContext);
                LOGGER.debug("Calculated attributes [{}] via attribute release policy [{}]", policyAttr, policy.getName());
                return policyAttr;
            }))
            .forEach(attributes::putAll);
        return attributes;
    }

    private Map<String, List<Object>> getAttributesAllowedForService(
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
            .attributeReleasePolicyPredicate(policy -> !(policy instanceof OidcRegisteredServiceAttributeReleasePolicy)
                || scopes.contains(((OidcRegisteredServiceAttributeReleasePolicy) policy).getScopeType()))
            .build();
        return oidcService.getAttributeReleasePolicy().getAttributes(releasePolicyContext);
    }
}
