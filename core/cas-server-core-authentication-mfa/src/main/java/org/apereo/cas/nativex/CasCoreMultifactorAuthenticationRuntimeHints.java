package org.apereo.cas.nativex;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasCoreMultifactorAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreMultifactorAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints,
            MultifactorAuthenticationTrigger.class,
            ChainingMultifactorAuthenticationProvider.class,
            MultifactorAuthenticationProviderBypassEvaluator.class
        );
        registerReflectionHints(hints, List.of(
            ChainingMultifactorAuthenticationProvider.class
        ));
    }
}
