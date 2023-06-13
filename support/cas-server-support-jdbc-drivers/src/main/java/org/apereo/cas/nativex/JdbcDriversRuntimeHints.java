package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link JdbcDriversRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JdbcDriversRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerResourceBundle("org.hsqldb.resources.sql-state-messages");
    }
}
