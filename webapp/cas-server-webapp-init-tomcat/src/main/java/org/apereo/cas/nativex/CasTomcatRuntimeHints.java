package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasTomcatRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class CasTomcatRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources()
            .registerResourceBundle("org/apache/tomcat/util/concurrent/LocalStrings");
    }
}
