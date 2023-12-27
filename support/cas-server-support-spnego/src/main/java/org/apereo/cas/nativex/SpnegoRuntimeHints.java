package org.apereo.cas.nativex;

import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoPrincipalResolver;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link SpnegoRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SpnegoRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, SpnegoCredential.class);

        registerReflectionHints(hints, List.of(
            SpnegoCredential.class,
            SpnegoPrincipalResolver.class
        ));
    }
}
