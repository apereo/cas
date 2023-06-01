package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link CasWebAppResourcesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppResourcesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerResource(new ClassPathResource("application.properties"));
        hints.resources().registerResource(new ClassPathResource("application.yml"));
        hints.resources().registerResource(new ClassPathResource("spring.properties"));
    }
}
