package org.apereo.cas.nativex;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordResetTokenCipherExecutor;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.CaptchaActivationStrategy;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasPasswordManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasPasswordManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies().registerJdkProxy(CaptchaActivationStrategy.class);

        hints.reflection().registerType(PasswordChangeRequest.class,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        hints.reflection().registerType(PasswordResetTokenCipherExecutor.class,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        hints.serialization().registerType(JsonResourcePasswordManagementService.JsonBackedAccount.class);
        hints.serialization().registerType(PasswordChangeRequest.class);
    }
}
