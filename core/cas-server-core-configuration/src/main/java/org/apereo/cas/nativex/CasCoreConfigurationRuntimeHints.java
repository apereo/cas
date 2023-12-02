package org.apereo.cas.nativex;

import org.apereo.cas.configuration.model.core.web.view.CustomLoginFieldViewProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasCoreConfigurationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreConfigurationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val entries = List.<Class>of(CustomLoginFieldViewProperties.class);
        entries.forEach(entry -> hints.serialization().registerType(entry));

    }
}
