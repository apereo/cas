package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.GeodeTicketDocument;
import lombok.val;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.cache.control.SerializableRegionRedundancyStatusImpl;
import org.apache.geode.management.JVMMetrics;
import org.apache.geode.management.MemberMXBean;
import org.apache.geode.management.internal.beans.MemberMBean;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasGeodeRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class CasGeodeRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasGeodeRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.resource().forResource("org/apache/geode/internal/GemFireVersion.properties").test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(GeodeTicketDocument.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(MemberMBean.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(JVMMetrics.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(MemberMXBean.class.getDeclaredMethod("getName")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(ObjectInputFilter.Config.class.getDeclaredMethod("createFilter", String.class)).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ObjectInputFilter.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onFieldAccess(DistributionConfig.class.getDeclaredField("NAME_NAME")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(DistributionConfig.class.getDeclaredMethod("setName", String.class)).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(SerializableRegionRedundancyStatusImpl.class.getDeclaredConstructor()).test(hints));
    }
}
