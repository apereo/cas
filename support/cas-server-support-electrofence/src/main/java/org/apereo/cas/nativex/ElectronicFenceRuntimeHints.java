package org.apereo.cas.nativex;

import org.apereo.cas.api.AuthenticationRequestRiskCalculator;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link ElectronicFenceRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class ElectronicFenceRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, List.of(AuthenticationRequestRiskCalculator.class));
    }
}
