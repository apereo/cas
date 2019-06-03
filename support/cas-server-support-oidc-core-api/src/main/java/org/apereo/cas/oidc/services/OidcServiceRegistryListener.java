package org.apereo.cas.oidc.services;

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
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;

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

    private final Collection<BaseOidcScopeAttributeReleasePolicy> userScopes;

    @Override
    public RegisteredService postLoad(final RegisteredService registeredService) {
        if (registeredService instanceof OidcRegisteredService) {
            return reconcile((OidcRegisteredService) registeredService);
        }
        return registeredService;
    }

    private RegisteredService reconcile(final OidcRegisteredService oidcService) {
        LOGGER.trace("Reconciling OpenId Connect scopes and claims for [{}]", oidcService.getServiceId());

        val definedServiceScopes = oidcService.getScopes();
        if (definedServiceScopes.isEmpty()) {
            LOGGER.trace("Registered service [{}] does not define any scopes to control attribute release policies. "
                + "CAS will allow the existing attribute release policies assigned to the service to operate without a scope.", oidcService.getServiceId());
            return oidcService;
        }

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
                    .filter(obj -> obj instanceof OidcCustomScopeAttributeReleasePolicy)
                    .map(t -> (OidcCustomScopeAttributeReleasePolicy) t)
                    .filter(t -> t.getScopeName().equals(givenScope.trim()))
                    .findFirst()
                    .ifPresent(userPolicy -> addAttributeReleasePolicy(policyChain, userPolicy, givenScope, oidcService));
            } else {
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
                        LOGGER.debug("Given scope [{}], service [{}] is marked to generate refresh tokens", givenScope, oidcService.getId());
                        oidcService.setGenerateRefreshToken(true);
                        break;
                    default:
                        LOGGER.debug("Scope [{}] is unsupported for service [{}]", givenScope, oidcService.getId());
                        break;
                }
            }
        });

        if (policyChain.getPolicies().isEmpty()) {
            LOGGER.debug("No attribute release policy could be determined based on given scopes. "
                + "No claims/attributes will be released to [{}]", oidcService.getServiceId());
            oidcService.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        } else {
            oidcService.setAttributeReleasePolicy(policyChain);
        }

        LOGGER.trace("Scope/claim reconciliation for service [{}] resulted in the following attribute release policy [{}]",
            oidcService.getServiceId(), oidcService.getAttributeReleasePolicy());

        return oidcService;
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
