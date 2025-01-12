package org.apereo.cas.nativex;

import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasMultitenancyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class CasMultitenancyRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, classLoader, TenantDefinition.class);
        registerReflectionHints(hints, TenantDefinition.class);
        registerReflectionHints(hints, TenantExtractor.class);
    }
}
