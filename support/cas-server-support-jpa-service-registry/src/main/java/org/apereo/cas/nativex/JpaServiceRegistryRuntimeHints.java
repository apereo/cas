package org.apereo.cas.nativex;

import org.apereo.cas.services.JpaRegisteredServiceEntity;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link JpaServiceRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JpaServiceRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(JpaRegisteredServiceEntity.class));

        registerSerializationHints(hints, JpaRegisteredServiceEntity.class);
    }
}
