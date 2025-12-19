package org.apereo.cas.support.events.audit;

import module java.base;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAuditActionContextRecordedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Events")
class CasAuditActionContextRecordedEventTests {

    @Test
    void verifyOperation() throws Throwable {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                val ctx = new AuditActionContext("casuser", "TEST", "TEST",
                    "CAS", LocalDateTime.now(Clock.systemUTC()),
                    new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "Paris"));
                new CasAuditActionContextRecordedEvent(this, ctx, null);
            }
        });
    }

}
