package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.javers.spring.mongodb.DBRefUnproxyObjectAccessHook;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JaversRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class JaversRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.reflection().registerType(DBRefUnproxyObjectAccessHook.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        registerReflectionHintsForConstructors(hints,
            findSubclassesInPackage(Object.class, "org.javers.core", "org.javers.repository", "org.javers.common"));
    }
}
