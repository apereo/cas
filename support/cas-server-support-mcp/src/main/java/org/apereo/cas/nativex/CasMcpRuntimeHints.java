package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasMcpRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMcpRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        ReflectionUtils.findSubclassesInPackage(Record.class, "org.apereo.cas.mcp")
            .stream()
            .filter(type -> Modifier.isPublic(type.getModifiers()))
            .forEach(type -> new BindingReflectionHintsRegistrar().registerReflectionHints(hints.reflection(), type));
    }
}
