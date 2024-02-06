package org.apereo.cas.nativex;

import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link SamlRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SamlRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, SamlService.class);
        registerReflectionHints(hints, SamlService.class);
    }
}
