package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link SamlIdPCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlIdPCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources().registerPattern("template-idp-metadata.vm");
    }
}
