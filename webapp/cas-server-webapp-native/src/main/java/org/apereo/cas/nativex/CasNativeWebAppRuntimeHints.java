package org.apereo.cas.nativex;

import org.apereo.cas.context.CasApplicationContextInitializer;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * This is {@link CasNativeWebAppRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeWebAppRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection()
            .registerType(CasApplicationContextInitializer.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
