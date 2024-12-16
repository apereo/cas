package org.apereo.cas.nativex;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
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
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSpringProxyHints(hints, DisposableBean.class, YubiKeyAccountRegistry.class);
    }
}


