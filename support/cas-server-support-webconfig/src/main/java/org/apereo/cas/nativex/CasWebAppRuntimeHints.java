package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * This is {@link CasWebAppRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val csrfTokens = findSubclassesInPackage(CsrfToken.class, CsrfToken.class.getPackageName());
        registerReflectionHints(hints, csrfTokens);
        registerSerializationHints(hints, csrfTokens);
    }
}
