package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.springframework.aot.hint.RuntimeHints;
import java.io.Serializable;

/**
 * This is {@link CasCookieRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCookieRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, CasCookieBuilder.class);
        registerSpringProxyHints(hints, Serializable.class, CasCookieBuilder.class);
    }
}
