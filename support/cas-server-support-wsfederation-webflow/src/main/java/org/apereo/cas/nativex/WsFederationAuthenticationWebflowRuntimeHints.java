package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.flow.WsFedClient;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link WsFederationAuthenticationWebflowRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WsFederationAuthenticationWebflowRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints, WsFedClient.class);
    }
}
