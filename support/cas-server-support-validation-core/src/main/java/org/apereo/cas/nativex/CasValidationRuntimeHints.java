package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.view.json.CasJsonServiceResponse;
import org.apereo.cas.web.view.json.CasJsonServiceResponseAuthenticationFailure;
import org.apereo.cas.web.view.json.CasJsonServiceResponseAuthenticationSuccess;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasValidationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasValidationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            CasJsonServiceResponse.class,
            CasJsonServiceResponseAuthenticationSuccess.class,
            CasJsonServiceResponseAuthenticationFailure.class));
    }
}
