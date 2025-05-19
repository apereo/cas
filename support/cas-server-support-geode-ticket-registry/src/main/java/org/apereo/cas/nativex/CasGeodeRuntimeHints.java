package org.apereo.cas.nativex;

import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apache.geode.internal.cache.InternalCacheBuilder;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasGeodeRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class CasGeodeRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, GeodeTicketDocument.class);
        registerSerializationHints(hints, InternalCacheBuilder.class);
    }
}
