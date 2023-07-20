package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasWebAppResourcesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppResourcesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources()
            .registerPattern("application.properties")
            .registerPattern("application.yml")
            .registerPattern("spring.properties")
            .registerPattern("services/*.json");
    }
}
