package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.services.JpaRegisteredServiceEntity;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JpaServiceRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JpaServiceRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(JpaRegisteredServiceEntity.class));

        registerSerializationHints(hints, JpaRegisteredServiceEntity.class);
    }
}
