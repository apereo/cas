package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link OidcFederationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcFederationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, List.of(
            OidcRegisteredService.class,
            OidcJsonWebKeyCacheKey.class,
            OidcJsonWebKeystoreEntity.class
        ));
        registerReflectionHints(hints, List.of(
            OidcJsonWebKeystoreEntity.class,
            OidcJsonWebKeystoreGeneratorService.class,
            OidcRegisteredService.class
        ));

        val releasePolicies = findSubclassesInPackage(OidcRegisteredServiceAttributeReleasePolicy.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, releasePolicies);
        registerSerializationHints(hints, releasePolicies);

        val entries = findSubclassesInPackage(OidcJsonWebKeystoreGeneratorService.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, entries);
        registerSpringProxyHints(hints, OidcJsonWebKeystoreGeneratorService.class, DisposableBean.class);
    }

}

