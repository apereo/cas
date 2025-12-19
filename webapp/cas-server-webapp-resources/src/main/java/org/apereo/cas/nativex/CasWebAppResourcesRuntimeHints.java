package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasWebAppResourcesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppResourcesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources()
            .registerPattern("application.properties")
            .registerPattern("application.yml")
            .registerPattern("spring.properties")
            .registerPattern("services/*.json");
    }
}
