package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apache.arrow.memory.AllocationManager;
import org.apache.arrow.memory.netty.DefaultAllocationManagerFactory;
import org.apache.arrow.memory.netty.NettyAllocationManager;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link InfluxDbRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class InfluxDbRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints,
            AllocationManager.Factory.class,
            DefaultAllocationManagerFactory.class,
            NettyAllocationManager.class);
    }
}
