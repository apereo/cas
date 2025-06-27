package org.apereo.cas.nativex;

import org.apereo.cas.configuration.model.core.web.view.CustomLoginFieldViewProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
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
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, CustomLoginFieldViewProperties.class);
        registerReflectionHints(hints, findSubclassesOf(BindContext.class));
    }
}
