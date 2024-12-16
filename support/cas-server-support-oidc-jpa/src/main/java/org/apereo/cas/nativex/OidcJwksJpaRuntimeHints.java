package org.apereo.cas.nativex;

import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link OidcJwksJpaRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcJwksJpaRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSpringProxyHints(hints, OidcJsonWebKeystoreGeneratorService.class, JpaPersistenceUnitProvider.class);
        registerReflectionHints(hints, List.of(JpaPersistenceUnitProvider.class));
    }

}
