package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JavaMelodyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class JavaMelodyRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources().registerResourceBundle("net.bull.javamelody.resource.translations");
        hints.resources().registerPattern("net/bull/javamelody/resource/**");
        registerReflectionHints(hints, List.of("com.sun.management.OperatingSystemMXBean",
            "com.sun.management.ThreadMXBean", "com.sun.management.UnixOperatingSystemMXBean"));
        registerReflectionHintsForMethodsAndFields(hints, List.of("net.bull.javamelody.internal.web.HtmlController",
            "net.bull.javamelody.internal.web.PdfController", "net.bull.javamelody.internal.web.SerializableController"));
        registerSerializationHints(hints, findSubclassesInPackage(Serializable.class, "net.bull.javamelody"));
    }
}
