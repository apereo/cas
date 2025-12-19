package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link YubiKeyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class YubiKeyRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, DisposableBean.class, YubiKeyAccountRegistry.class);
    }
}


