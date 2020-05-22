package org.apereo.cas.audit.spi;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseAuditConfigurationTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseAuditConfigurationTests {
    public abstract AuditTrailManager getAuditTrailManager();

    @BeforeEach
    public void onSetUp() {
        val auditTrailManager = getAuditTrailManager();
        auditTrailManager.removeAll();
    }

    @Test
    public void verifyAuditManager() {
        val auditTrailManager = getAuditTrailManager();
        val time = LocalDate.now(ZoneOffset.UTC).minusDays(2);
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", new Date(), "1.2.3.4",
            "1.2.3.4");
        auditTrailManager.record(ctx);
        val results = auditTrailManager.getAuditRecordsSince(time);
        assertFalse(results.isEmpty());
        auditTrailManager.removeAll();
    }
}
