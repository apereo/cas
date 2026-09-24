package org.apereo.cas.oidc.vc.services;

import module java.base;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialPolicyUtils}, which decides which of the credential
 * configurations the issuer publishes a given relying party may actually obtain.
 * <p>
 * The credential configurations in {@code cas.authn.oidc.vc.issuer.credential-configurations} describe
 * what the issuer is able to mint; they say nothing about who may ask for what. Without a per-service
 * decision, any registered client that can reach the offer or authorization endpoints can obtain every
 * credential the deployment defines, which for an employee or student credential is the whole game.
 * {@link org.apereo.cas.services.RegisteredServiceOidcVerifiableCredentialsPolicy} is where a service
 * narrows that down, and this is where the narrowing is applied.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
@Slf4j
public class OidcVerifiableCredentialPolicyUtils {

    /**
     * Resolve the credential configuration ids the given service may obtain.
     * <p>
     * The result is always a subset of what the issuer publishes: a policy that names a credential the
     * deployment does not define cannot conjure it into existence. A service with no verifiable
     * credentials policy, or one whose policy lists no credential types, is left with everything the
     * issuer offers, which keeps existing deployments working exactly as they did.
     *
     * @param registeredService      the registered service, which may be absent or not an OpenID Connect service
     * @param issuerConfigurationIds the credential configuration ids the issuer publishes
     * @return the allowed credential configuration ids, never null
     */
    public Set<String> resolveAllowedCredentialConfigurationIds(
        final @Nullable RegisteredService registeredService,
        final Set<String> issuerConfigurationIds) {
        if (!(registeredService instanceof final OidcRegisteredService oidcRegisteredService)) {
            LOGGER.trace("Service [{}] is not an OpenID Connect service; all credential configurations apply",
                registeredService);
            return Set.copyOf(issuerConfigurationIds);
        }
        val policy = oidcRegisteredService.getVerifiableCredentialsPolicy();
        if (policy == null) {
            LOGGER.trace("Service [{}] defines no verifiable credentials policy; all credential configurations apply",
                oidcRegisteredService.getName());
            return Set.copyOf(issuerConfigurationIds);
        }
        val allowed = issuerConfigurationIds
            .stream()
            .filter(policy::isCredentialTypeAllowed)
            .collect(Collectors.toSet());
        LOGGER.debug("Service [{}] is allowed credential configurations [{}] out of [{}]",
            oidcRegisteredService.getName(), allowed, issuerConfigurationIds);
        return allowed;
    }
}
