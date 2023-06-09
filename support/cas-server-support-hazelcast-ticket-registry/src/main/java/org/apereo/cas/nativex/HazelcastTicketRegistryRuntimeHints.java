package org.apereo.cas.nativex;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.hazelcast.sql.impl.SqlServiceImpl;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link HazelcastTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class HazelcastTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(SqlServiceImpl.class));
        FunctionUtils.doAndHandle(__ -> {
            var clazz = ClassUtils.getClass("com.hazelcast.shaded.org.jctools.queues.ConcurrentCircularArrayQueueL0Pad", false);
            registerReflectionHints(hints,
                findSubclassesInPackage(clazz, "com.hazelcast.shaded.org.jctools"));
        });
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        entries.forEach(el -> hints.reflection().registerType((Class) el,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS));
    }
}
