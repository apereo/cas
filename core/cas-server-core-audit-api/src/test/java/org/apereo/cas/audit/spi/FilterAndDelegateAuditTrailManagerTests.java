package org.apereo.cas.audit.spi;

import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FilterAndDelegateAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class FilterAndDelegateAuditTrailManagerTests {

    @Test
    public void verifyOperationForAllActions() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", new Date(), "1.2.3.4",
            "1.2.3.4");
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(Collections.singletonList(mock), Collections.singletonList("*"));
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
    }

    @Test
    public void verifyOperationForAllSupportedActions() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", new Date(), "1.2.3.4",
            "1.2.3.4");
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(Collections.singletonList(mock), Collections.singletonList("TEST.*"));
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
    }

    @Test
    public void verifyOperationForUnmatchedActions() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", new Date(), "1.2.3.4",
            "1.2.3.4");
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(Collections.singletonList(mock), Collections.singletonList("PASSED.*"));
        mgr.record(ctx);
        assertTrue(mock.getAuditRecords().isEmpty());
    }

    @Test
    public void verifyAuditRecordsSinceDate() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS",
            DateTimeUtils.dateOf(LocalDateTime.now(ZoneOffset.UTC).plusDays(1)),
            "1.2.3.4",
            "1.2.3.4");
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(Collections.singletonList(mock), Collections.singletonList("TEST.*"));
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
        assertEquals(1, mock.getAuditRecordsSince(LocalDate.now(ZoneOffset.UTC)).size());
    }
}
