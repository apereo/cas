package org.apereo.cas.nativex;

import org.apereo.cas.logout.LogoutPostProcessor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasCoreLogoutRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CasCoreLogoutRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, List.of(LogoutPostProcessor.class));
    }
}

