package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.reflections.Reflections;
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
    private final Map<String, BaseOidcScopeAttributeReleasePolicy> attributeReleasePolicies = new LinkedHashMap<>();

    private final PrincipalFactory principalFactory;

    private final CasConfigurationProperties casProperties;

    private final Collection<OidcCustomScopeAttributeReleasePolicy> userScopes;

    public OidcProfileScopeToAttributesFilter(final PrincipalFactory principalFactory,
                                              final CasConfigurationProperties casProperties,
                                              final OidcAttributeReleasePolicyFactory oidcAttributeReleasePolicyFactory) {
        this.principalFactory = principalFactory;
        this.casProperties = casProperties;
        this.userScopes = oidcAttributeReleasePolicyFactory.getUserDefinedScopes();
        configureAttributeReleasePoliciesByScope();
    }

    @Override
    public Principal filter(final Service service,
                            final Principal profile,
                            final RegisteredService registeredService,
                            final WebContext context,
                            final OAuth20AccessToken accessToken) {
        val principal = super.filter(service, profile, registeredService, context, accessToken);
        if (registeredService instanceof OidcRegisteredService) {
            val scopes = new LinkedHashSet<>(accessToken.getScopes());
            if (!scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
                LOGGER.warn("Request does not indicate a scope [{}] that can identify an OpenID Connect request. "
                            + "This is a REQUIRED scope that MUST be present in the request. Given its absence, "
                            + "CAS will not process any attribute claims and will return the authenticated principal as is.", scopes);
                return principal;
            }

            val oidcService = (OidcRegisteredService) registeredService;
            val attributes = getAttributesAllowedForService(scopes, principal, service, oidcService, accessToken);
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
                                                                final OAuth20AccessToken accessToken,
                                                                final Principal principal,
                                                                final Map<String, List<Object>> attributes) {
        val userInfo = OAuth20Utils.parseUserInfoRequestClaims(accessToken);
        if (userInfo.isEmpty()) {
            LOGGER.trace("No userinfo requested claims are available");
            return;
        }

        val principalAttributes = accessToken.getTicketGrantingTicket().getAuthentication().getPrincipal().getAttributes();
        LOGGER.debug("Requested user-info claims [{}] are compared against principal attributes [{}]", userInfo, principalAttributes);
        userInfo
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
                                                                final OAuth20AccessToken accessToken) {
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
            .filter(this.attributeReleasePolicies::containsKey)
            .map(s -> {
                val policy = attributeReleasePolicies.get(s);
                val policyAttr = policy.getAttributes(principal, service, registeredService);
                LOGGER.debug("Calculated attributes [{}] via attribute release policy [{}]", policyAttr, policy.getName());
                return policyAttr;
            })
            .forEach(attributes::putAll);
        return attributes;
    }

    /**
     * Configure attribute release policies by scope.
     */
    protected void configureAttributeReleasePoliciesByScope() {
        val oidc = casProperties.getAuthn().getOidc();
        val packageName = BaseOidcScopeAttributeReleasePolicy.class.getPackage().getName();
        val reflections = new Reflections(new ConfigurationBuilder()
            .filterInputsBy(new FilterBuilder().includePackage(packageName))
            .setUrls(ClasspathHelper.forPackage(packageName)));

        val subTypes = reflections.getSubTypesOf(BaseOidcScopeAttributeReleasePolicy.class);
        subTypes.forEach(Unchecked.consumer(t -> {
            if (ClassUtils.hasConstructor(t)) {
                val ex = t.getDeclaredConstructor().newInstance();
                if (oidc.getDiscovery().getScopes().contains(ex.getScopeType())) {
                    LOGGER.trace("Found standard OpenID Connect scope [{}] to filter attributes", ex.getScopeType());
                    this.attributeReleasePolicies.put(ex.getScopeType(), ex);
                } else {
                    LOGGER.debug("OpenID Connect scope [{}] is not configured for use and will be ignored", ex.getScopeType());
                }
            }
        }));

        if (!userScopes.isEmpty()) {
            LOGGER.debug("Configuring attributes release policies for user-defined scopes [{}]", userScopes);
            userScopes.forEach(t -> attributeReleasePolicies.put(t.getScopeName(), t));
        }
    }

    /**
     * Get all attributes allowed by the service.
     * If a service is registered with {@code scopes}, get attributes allowed as per defined release policies for scopes.
     * If a service registered with no {@code scopes}, then service {@code attributeReleasePolicy} will be used to get allowed attributes.
     *
     * @param scopes      the scopes
     * @param principal   the principal
     * @param service     the service
     * @param oidcService the registered service
     * @param accessToken the access token
     * @return Attributes allowed by the service
     */
    private Map<String, List<Object>> getAttributesAllowedForService(final Collection<String> scopes,
                                                                     final Principal principal,
                                                                     final Service service,
                                                                     final OidcRegisteredService oidcService,
                                                                     final OAuth20AccessToken accessToken) {
        val serviceScopes = oidcService.getScopes();
        LOGGER.trace("Scopes assigned to service definition [{}] are [{}]", oidcService.getName(), serviceScopes);
        val scopeFree = serviceScopes.isEmpty() || (serviceScopes.size() == 1
                                                    && serviceScopes.contains(OidcConstants.StandardScopes.OPENID.getScope()));
        if (!scopeFree) {
            scopes.retainAll(serviceScopes);
            LOGGER.trace("Service definition [{}] will filter attributes based on scopes [{}]", oidcService.getName(), scopes);
            return filterAttributesByScope(scopes, principal, service, oidcService, accessToken);
        }
        LOGGER.trace("Service definition [{}] invokes the assigned attribute release policy without using scopes", oidcService.getName());
        return oidcService.getAttributeReleasePolicy().getAttributes(principal, service, oidcService);
    }
}
