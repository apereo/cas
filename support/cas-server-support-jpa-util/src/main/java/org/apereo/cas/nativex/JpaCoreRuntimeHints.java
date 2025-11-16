package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.orm.jpa.JpaVendorAdapter;

/**
 * This is {@link JpaCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class JpaCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, JpaVendorAdapter.class);
    }
}

