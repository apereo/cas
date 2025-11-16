package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.integration.jdbc.lock.LockRepository;

/**
 * This is {@link JpaRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class JpaRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, LockRepository.class);
    }
}


