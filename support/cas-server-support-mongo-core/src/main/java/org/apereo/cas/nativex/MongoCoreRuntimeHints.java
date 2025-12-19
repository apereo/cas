package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

/**
 * This is {@link MongoCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class MongoCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        val entries = List.<Class>of(WriteConcern.class, ReadConcern.class, AggregationOperation.class);
        registerReflectionHints(hints, entries);
    }
}
