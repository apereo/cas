package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.scopes.OidcAttributeReleasePolicyFactory;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceChainingAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link OidcServiceRegistryListener}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcServiceRegistryListener implements ServiceRegistryListener {
    private static final long serialVersionUID = -2492163812728091841L;

    private final OidcAttributeReleasePolicyFactory attributeReleasePolicyFactory;

    private static void addAttributeReleasePolicy(
        final RegisteredServiceChainingAttributeReleasePolicy chain,
        final BaseOidcScopeAttributeReleasePolicy policyToAdd,
        final String givenScope,
        final OidcRegisteredService registeredService) {
        LOGGER.debug("Mapped [{}] to attribute release policy [{}]",
            givenScope, policyToAdd.getClass().getSimpleName());

        val policy = registeredService.getAttributeReleasePolicy();
        val matchingPolicies = new ArrayList<RegisteredServiceAttributeReleasePolicy>();

        if (policy instanceof RegisteredServiceChainingAttributeReleasePolicy) {
            val chainedPolicy = (RegisteredServiceChainingAttributeReleasePolicy) policy;
            matchingPolicies.addAll(chainedPolicy.getPolicies()
                .stream()
                .filter(p -> p instanceof OidcRegisteredServiceAttributeReleasePolicy)
                .map(OidcRegisteredServiceAttributeReleasePolicy.class::cast)
                .filter(p -> p.getScopeType().equalsIgnoreCase(givenScope))
                .collect(Collectors.toList()));
        } else if (policy instanceof OidcRegisteredServiceAttributeReleasePolicy) {
            val oidcPolicy = (OidcRegisteredServiceAttributeReleasePolicy) policy;
            if (oidcPolicy.getScopeType().equalsIgnoreCase(givenScope)) {
                matchingPolicies.add(oidcPolicy);
            }
        }

        if (matchingPolicies.isEmpty()) {
            policyToAdd.setConsentPolicy(policy.getConsentPolicy());
            policyToAdd.setPrincipalAttributesRepository(policy.getPrincipalAttributesRepository());
            chain.addPolicies(policyToAdd);
        } else {
            chain.addPolicies(matchingPolicies);
        }
    }

    @Override
    public RegisteredService postLoad(final RegisteredService registeredService) {
        if (registeredService instanceof OidcRegisteredService) {
            return reconcile((OidcRegisteredService) registeredService);
        }
        return registeredService;
    }

    protected RegisteredService reconcile(final OidcRegisteredService oidcService) {
        LOGGER.trace("Reconciling OpenId Connect scopes and claims for [{}]", oidcService.getServiceId());

        val definedServiceScopes = oidcService.getScopes();
        if (definedServiceScopes.isEmpty()) {
            LOGGER.trace("Registered service [{}] does not define any scopes to control attribute release policies. "
                         + "CAS will allow the existing attribute release policies assigned to the service to operate without a scope.",
                oidcService.getServiceId());
            return oidcService;
        }

        val userScopes = attributeReleasePolicyFactory.getUserDefinedScopes();
        val customClaims = new ArrayList<String>();
        val policyChain = new ChainingAttributeReleasePolicy();

        definedServiceScopes.forEach(givenScope -> {
            LOGGER.trace("Reviewing scope [{}] for [{}]", givenScope, oidcService.getServiceId());

            val userDefinedScope = Arrays.stream(OidcConstants.StandardScopes.values())
                .noneMatch(scope -> scope.getScope().trim().equalsIgnoreCase(givenScope.trim()));
            if (userDefinedScope) {
                LOGGER.debug("[{}] appears to be a user-defined scope and does not match any of the predefined standard scopes. "
                             + "Checking [{}] against user-defined scopes provided as [{}]", givenScope, givenScope, userScopes);
                userScopes
                    .stream()
                    .filter(t -> t.getScopeName().equals(givenScope.trim()))
                    .findFirst()
                    .ifPresentOrElse(
                        userPolicy -> addAttributeReleasePolicy(policyChain, userPolicy, givenScope, oidcService),
                        () -> customClaims.add(givenScope.trim()));
            } else {
                val scope = OidcConstants.StandardScopes.valueOf(givenScope.trim().toUpperCase());
                switch (scope) {
                    case EMAIL:
                    case ADDRESS:
                    case PROFILE:
                    case PHONE:
                        val policyToAdd = attributeReleasePolicyFactory.get(scope);
                        addAttributeReleasePolicy(policyChain, policyToAdd, givenScope, oidcService);
                        break;
                    case OPENID:
                        LOGGER.debug("Scope [{}] is found for service [{}]", givenScope, oidcService.getId());
                        break;
                    case OFFLINE_ACCESS:
                        LOGGER.debug("Given scope [{}], service [{}] is marked to generate refresh tokens", givenScope, oidcService.getId());
                        oidcService.setGenerateRefreshToken(true);
                        break;
                }
            }
        });
        if (!customClaims.isEmpty()) {
            val userPolicy = attributeReleasePolicyFactory.custom(OidcConstants.CUSTOM_SCOPE_TYPE, customClaims);
            addAttributeReleasePolicy(policyChain, userPolicy, userPolicy.getScopeName(), oidcService);
        }

        val scopeFree = definedServiceScopes.isEmpty() || (definedServiceScopes.size() == 1
                                                           && definedServiceScopes.contains(OidcConstants.StandardScopes.OPENID.getScope()));
        if (scopeFree) {
            LOGGER.trace("Service definition [{}] will use the assigned attribute release policy without scopes", oidcService.getName());

            if (oidcService.getAttributeReleasePolicy() instanceof RegisteredServiceChainingAttributeReleasePolicy) {
                val chain = (RegisteredServiceChainingAttributeReleasePolicy) oidcService.getAttributeReleasePolicy();
                policyChain.addPolicies(chain.getPolicies().toArray(new RegisteredServiceAttributeReleasePolicy[0]));
            } else {
                policyChain.addPolicies(oidcService.getAttributeReleasePolicy());
            }
        }

        if (policyChain.getPolicies().isEmpty()) {
            LOGGER.debug("No attribute release policy could be determined based on given scopes. "
                         + "No claims/attributes will be released to [{}]", oidcService.getServiceId());
            oidcService.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        } else {
            if (policyChain.size() == 1) {
                oidcService.setAttributeReleasePolicy(policyChain.getPolicies().get(0));
            } else {
                oidcService.setAttributeReleasePolicy(policyChain);
            }
        }

        LOGGER.trace("Scope/claim reconciliation for service [{}] resulted in the following attribute release policy [{}]",
            oidcService.getServiceId(), oidcService.getAttributeReleasePolicy());

        return oidcService;
    }
}
