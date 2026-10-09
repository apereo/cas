package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.fusesource.jansi.AnsiConsole;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link LogbackRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class LogbackRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHintForDeclaredMethod(hints, AnsiConsole.class, "out");
        registerReflectionHintForDeclaredMethod(hints, AnsiConsole.class, "err");
    }
}
