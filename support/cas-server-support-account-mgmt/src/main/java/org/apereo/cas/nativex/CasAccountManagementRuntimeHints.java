package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasAccountManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasAccountManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        val entries = List.<Class>of(AccountRegistrationProvisionerConfigurer.class);
        registerProxyHints(hints, entries);
        registerReflectionHints(hints, entries);
    }
}
