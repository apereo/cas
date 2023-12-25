package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.BrowserStorage;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

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
        hints.serialization()
            .registerType(BrowserStorage.class);

        hints.reflection()
            .registerType(
                TypeReference.of("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler"),
                MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_METHODS, MemberCategory.INTROSPECT_DECLARED_METHODS);
    }
}
