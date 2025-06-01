package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.orm.jpa.JpaVendorAdapter;

/**
 * This is {@link JpaCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class JpaCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, JpaVendorAdapter.class);
    }
}

