package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

/**
 * This is {@link CasCoreWebRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreWebRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerResourceBundle("cas_common_messages");
        registerSerializationHints(hints, DefaultBrowserStorage.class);
        registerReflectionHints(hints, List.of(TypeReference.of("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler")));
    }
}
