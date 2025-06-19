package org.apereo.cas.oidc.scopes;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAssuranceScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcOfflineAccessScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcOpenIdScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.util.ClassUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultOidcAttributeReleasePolicyFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class DefaultOidcAttributeReleasePolicyFactory implements OidcAttributeReleasePolicyFactory {
    protected final Map<String, BaseOidcScopeAttributeReleasePolicy> attributeReleasePoliciesByScope = new ConcurrentHashMap<>();

    protected final CasConfigurationProperties casProperties;

    public DefaultOidcAttributeReleasePolicyFactory(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
        val oidc = casProperties.getAuthn().getOidc();
        val packageName = BaseOidcScopeAttributeReleasePolicy.class.getPackage().getName();

        val subTypes = ReflectionUtils.findSubclassesInPackage(BaseOidcScopeAttributeReleasePolicy.class, packageName);
        subTypes.forEach(Unchecked.consumer(type -> {
            if (ClassUtils.hasConstructor(type)) {
                val policyInstance = type.getDeclaredConstructor().newInstance();
                if (oidc.getDiscovery().getScopes().contains(policyInstance.getScopeType())) {
                    LOGGER.trace("Found standard OpenID Connect scope [{}] to filter attributes", policyInstance.getScopeType());
                    attributeReleasePoliciesByScope.put(policyInstance.getScopeType(), policyInstance);
                } else {
                    LOGGER.debug("OpenID Connect scope [{}] is not configured for use and will be ignored", policyInstance.getScopeType());
                }
            }
        }));
    }

    @Override
    public BaseOidcScopeAttributeReleasePolicy get(final OidcConstants.StandardScopes scope) {
        return switch (scope) {
            case EMAIL -> new OidcEmailScopeAttributeReleasePolicy();
            case ADDRESS -> new OidcAddressScopeAttributeReleasePolicy();
            case OPENID, DEVICE_SSO -> new OidcOpenIdScopeAttributeReleasePolicy();
            case PHONE -> new OidcPhoneScopeAttributeReleasePolicy();
            case PROFILE -> new OidcProfileScopeAttributeReleasePolicy();
            case ASSURANCE -> new OidcAssuranceScopeAttributeReleasePolicy();
            case OFFLINE_ACCESS -> new OidcOfflineAccessScopeAttributeReleasePolicy();
        };
    }

    @Override
    public OidcCustomScopeAttributeReleasePolicy custom(final String name, final List<String> allowedAttributes) {
        return new OidcCustomScopeAttributeReleasePolicy(name, allowedAttributes);
    }

    @Override
    public Collection<OidcCustomScopeAttributeReleasePolicy> getUserDefinedScopes() {
        return from(casProperties.getAuthn().getOidc().getCore().getUserDefinedScopes());
    }

    @Override
    public Map<String, BaseOidcScopeAttributeReleasePolicy> resolvePolicies(final OidcRegisteredService registeredService) {
        val policies = new HashMap<>(attributeReleasePoliciesByScope);

        val userScopes = getUserDefinedScopes();
        LOGGER.debug("Configuring attributes release policies for user-defined scopes [{}]", userScopes);
        userScopes.forEach(us -> policies.put(us.getScopeName(), us));

        LOGGER.debug("Configuring attributes release policies for user-defined scopes specified for service [{}]", registeredService.getName());
        val listOfOidcPolicies = new ArrayList<OidcRegisteredServiceAttributeReleasePolicy>();
        if (registeredService.getAttributeReleasePolicy() instanceof final ChainingAttributeReleasePolicy chain) {
            listOfOidcPolicies.addAll(chain.getPolicies()
                .stream()
                .filter(OidcRegisteredServiceAttributeReleasePolicy.class::isInstance)
                .map(OidcRegisteredServiceAttributeReleasePolicy.class::cast)
                .toList());
        } else if (registeredService.getAttributeReleasePolicy() instanceof final OidcRegisteredServiceAttributeReleasePolicy policy) {
            listOfOidcPolicies.add(policy);
        }
        listOfOidcPolicies
            .stream()
            .filter(OidcCustomScopeAttributeReleasePolicy.class::isInstance)
            .map(OidcCustomScopeAttributeReleasePolicy.class::cast)
            .filter(policy -> !policies.containsKey(policy.getScopeName()))
            .forEach(policy -> policies.put(policy.getScopeName(), policy));
        listOfOidcPolicies
            .stream()
            .filter(OidcScopeFreeAttributeReleasePolicy.class::isInstance)
            .map(OidcScopeFreeAttributeReleasePolicy.class::cast)
            .forEach(policy -> policies.put(UUID.randomUUID().toString(), policy));

        LOGGER.debug("Final set of scopes mapped to attribute release policies are [{}]", policies.keySet());
        return policies;
    }
}
