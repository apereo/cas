package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.hazelcast.instance.GeneratedBuildProperties;
import com.hazelcast.internal.cluster.Versions;
import com.hazelcast.internal.dynamicconfig.DynamicConfigurationAwareConfig;
import com.hazelcast.internal.partition.impl.MigrationStats;
import com.hazelcast.internal.serialization.DataSerializerHook;
import com.hazelcast.logging.LoggerFactory;
import com.hazelcast.map.IMap;
import com.hazelcast.query.extractor.ValueExtractor;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.spi.properties.HazelcastProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link HazelcastCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class HazelcastCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints,
            List.of(
                HazelcastProperty.class,
                ClusterProperty.class,
                GeneratedBuildProperties.class,
                DynamicConfigurationAwareConfig.class,
                Versions.class,
                MigrationStats.class,
                "com.hazelcast.map.impl.record.CachedDataRecordWithStats",
                "com.hazelcast.map.impl.record.CachedSimpleRecord"
            )
        );
        registerReflectionHints(hints,
            findSubclassesInPackage(LoggerFactory.class, "com.hazelcast.logging"));
        registerReflectionHints(hints,
            findSubclassesInPackage(IMap.class, "com.hazelcast"));
        registerReflectionHints(hints,
            findSubclassesInPackage(AbstractQueue.class, "com.hazelcast"));
        registerReflectionHints(hints,
            findSubclassesInPackage(DataSerializerHook.class, "com.hazelcast"));
        registerReflectionHints(hints,
            findSubclassesInPackage(ValueExtractor.class, "com.hazelcast"));

        registerProxyHints(hints, List.of(IMap.class));

        hints.resources()
            .registerPattern("META-INF/services/com.hazelcast.DataSerializerHook")
            .registerPattern("META-INF/services/com.hazelcast.shaded.com.fasterxml.jackson.core.ObjectCodec");
    }

}
