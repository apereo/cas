package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link OidcJwksJpaRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcJwksJpaRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, OidcJsonWebKeystoreGeneratorService.class, JpaPersistenceUnitProvider.class);
        registerReflectionHints(hints, List.of(JpaPersistenceUnitProvider.class));
    }

}
