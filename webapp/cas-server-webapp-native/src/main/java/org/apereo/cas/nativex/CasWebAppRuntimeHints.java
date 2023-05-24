package org.apereo.cas.nativex;

import org.apereo.cas.context.CasApplicationContextInitializer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasWebAppRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(CasApplicationContextInitializer.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
