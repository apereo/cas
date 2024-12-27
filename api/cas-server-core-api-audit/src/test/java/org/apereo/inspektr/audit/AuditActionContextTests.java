package org.apereo.inspektr.audit;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuditActionContextTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class AuditActionContextTests {
    @Test
    void verifyAuditContext() {
        val original = new AuditActionContext()
            .withPrincipal("casuser")
            .withResourceOperatedUpon("Resource")
            .withWhenActionWasPerformed(LocalDateTime.now(Clock.systemUTC()));
        val copy = new AuditActionContext(original);
        assertEquals(copy, original);
    }
}
