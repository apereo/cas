package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.geode.cache.query.internal.parse.GemFireAST;
import org.apache.geode.distributed.internal.AbstractDistributionConfig;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.cache.InternalCacheBuilder;
import org.apache.geode.internal.cache.UserSpecifiedRegionAttributes;
import org.apache.geode.internal.serialization.DataSerializableFixedID;
import org.apache.geode.logging.internal.log4j.api.LogService;
import org.apache.geode.management.MemberMXBean;
import org.apache.geode.management.RegionMXBean;
import org.apache.geode.management.internal.beans.MemberMBean;
import org.apache.geode.management.internal.beans.RegionMBean;
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
        hints.resources().registerPattern("org/apache/geode/internal/GemFireVersion.properties");
        registerReflectionHints(hints, LogService.class, MemberMBean.class, MemberMXBean.class, RegionMBean.class, RegionMXBean.class);
        val managementTypes = Stream.of(MemberMXBean.class, RegionMXBean.class)
            .flatMap(type -> Arrays.stream(type.getMethods()))
            .map(Method::getReturnType)
            .map(type -> type.isArray() ? type.getComponentType() : type)
            .filter(type -> type.getPackageName().startsWith("org.apache.geode.management"))
            .distinct()
            .toList();
        registerReflectionHints(hints, managementTypes);
        registerReflectionHintsForMethodsAndFields(hints,
            List.of(DistributionConfig.class, AbstractDistributionConfig.class, UserSpecifiedRegionAttributes.class));
        registerReflectionHints(hints, ObjectInputFilter.class, ObjectInputFilter.Config.class,
            ObjectInputFilter.FilterInfo.class, ObjectInputFilter.Status.class, ObjectInputStream.class);
        registerProxyHints(hints, ObjectInputFilter.class);
        registerReflectionHints(hints, GemFireAST.class);
        registerReflectionHints(hints, findSubclassesInPackage(GemFireAST.class, GemFireAST.class.getPackageName()));

        val subclasses = findSubclassesInPackage(DataSerializableFixedID.class, "org.apache.geode");
        registerReflectionHints(hints, subclasses);
        registerSerializationHints(hints, subclasses);

        registerSerializationHints(hints, GeodeTicketDocument.class);
        registerSerializationHints(hints, InternalCacheBuilder.class);

    }
}
