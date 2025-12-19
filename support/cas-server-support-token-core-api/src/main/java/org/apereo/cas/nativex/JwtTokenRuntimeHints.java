package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JwtTokenRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JwtTokenRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            JwtTicketCipherExecutor.class
        ));
    }
}

