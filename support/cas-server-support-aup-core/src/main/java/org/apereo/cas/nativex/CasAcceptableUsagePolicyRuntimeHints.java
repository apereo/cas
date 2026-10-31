package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.aup.TenantAcceptableUsagePolicyRepositoryBuilder;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasAcceptableUsagePolicyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasAcceptableUsagePolicyRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, TenantAcceptableUsagePolicyRepositoryBuilder.class);
    }
}
