package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link AmqpTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class AmqpTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, ClusterTopologyManager.class);
    }
}
