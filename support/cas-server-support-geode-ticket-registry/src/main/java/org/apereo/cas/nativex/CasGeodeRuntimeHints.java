package org.apereo.cas.nativex;

import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.geode.internal.cache.InternalCacheBuilder;
import org.apache.geode.internal.serialization.DataSerializableFixedID;
import org.apache.geode.logging.internal.log4j.api.LogService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasGeodeRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class CasGeodeRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, LogService.class);

        val subclasses = findSubclassesOf(DataSerializableFixedID.class);
        registerReflectionHints(hints, subclasses);
        registerSerializationHints(hints, subclasses);

        registerSerializationHints(hints, GeodeTicketDocument.class);
        registerSerializationHints(hints, InternalCacheBuilder.class);

    }
}
