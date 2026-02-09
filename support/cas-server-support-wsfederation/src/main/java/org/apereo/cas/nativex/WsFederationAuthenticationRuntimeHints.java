package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link WsFederationAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class WsFederationAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(WsFederationCredentialsToPrincipalResolver.class));
    }

}
