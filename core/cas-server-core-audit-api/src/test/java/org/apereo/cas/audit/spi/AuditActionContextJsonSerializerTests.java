package org.apereo.cas.audit.spi;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuditActionContextJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
class AuditActionContextJsonSerializerTests {
    @Test
    void verifyOperation() throws Throwable {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
        val serializer = new AuditActionContextJsonSerializer();
        val result = serializer.toString(ctx);
        assertNotNull(result);
        val audit = serializer.from(result);
        assertNotNull(audit);
    }
}
