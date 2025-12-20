package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JsonServiceRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JsonServiceRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
    }
}

