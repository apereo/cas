package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.configuration.model.core.web.view.CustomLoginFieldViewProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.boot.context.properties.bind.BindContext;

/**
 * This is {@link CasCoreConfigurationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreConfigurationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, CustomLoginFieldViewProperties.class);
        registerReflectionHints(hints, findSubclassesOf(BindContext.class));
    }
}
