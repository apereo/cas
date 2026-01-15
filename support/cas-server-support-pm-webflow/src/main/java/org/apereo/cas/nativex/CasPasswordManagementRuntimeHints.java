package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasPasswordManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasPasswordManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, CaptchaActivationStrategy.class);

        registerReflectionHints(hints, List.of(
            PasswordChangeRequest.class,
            PasswordResetTokenCipherExecutor.class));
        registerSerializationHints(hints, JsonResourcePasswordManagementService.JsonBackedAccount.class, PasswordChangeRequest.class);
    }
}
