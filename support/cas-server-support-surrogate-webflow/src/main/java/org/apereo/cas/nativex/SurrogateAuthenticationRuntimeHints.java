package org.apereo.cas.nativex;

import org.apereo.cas.authentication.SurrogatePrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link SurrogateAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SurrogateAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, SurrogateCredentialTrait.class);

        registerReflectionHints(hints, List.of(
            SurrogateCredentialTrait.class,
            SurrogatePrincipalResolver.class
        ));
    }
}
