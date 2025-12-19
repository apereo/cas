package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import org.apereo.cas.nativex.JdbcAuditRuntimeHintsRegistrar;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAuditRuntimeHintsRegistrarTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class JdbcAuditRuntimeHintsRegistrarTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new JdbcAuditRuntimeHintsRegistrar().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(AuditTrailEntity.class).test(hints));
    }
}
