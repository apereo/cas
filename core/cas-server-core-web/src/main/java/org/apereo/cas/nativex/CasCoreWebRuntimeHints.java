package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasCoreWebRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreWebRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerResourceBundle("cas_common_messages");
    }
}
