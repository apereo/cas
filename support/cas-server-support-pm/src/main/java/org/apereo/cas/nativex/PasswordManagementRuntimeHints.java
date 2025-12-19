package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.pm.PasswordManagementExecutionPlan;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link PasswordManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class PasswordManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, PasswordManagementExecutionPlan.class);
    }
}

