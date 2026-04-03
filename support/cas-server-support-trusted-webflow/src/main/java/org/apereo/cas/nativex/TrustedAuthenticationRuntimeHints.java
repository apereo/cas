package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link TrustedAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class TrustedAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, PrincipalBearingCredential.class);

        registerReflectionHints(hints, List.of(
            PrincipalBearingCredential.class,
            PrincipalBearingPrincipalResolver.class
        ));
    }
}
