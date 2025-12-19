package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link SurrogateAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SurrogateAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, SurrogateCredentialTrait.class);

        registerReflectionHints(hints, List.of(
            SurrogateCredentialTrait.class,
            SurrogatePrincipalResolver.class
        ));
    }
}
