package org.apereo.cas.audit.spi;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.FilterAndDelegateAuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FilterAndDelegateAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Audits")
class FilterAndDelegateAuditTrailManagerTests {

    @Test
    void verifyExcludeOperationForAllActions() {
        val ctx = getAuditActionContext();
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(List.of(mock), List.of("*"), List.of("TES.+"));
        mgr.record(ctx);
        assertTrue(mock.getAuditRecords().isEmpty());
    }

    private static AuditActionContext getAuditActionContext() {
        return new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
    }

    @Test
    void verifyOperationForAllActions() {
        val ctx = getAuditActionContext();
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(List.of(mock), List.of("*"), List.of());
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
    }

    @Test
    void verifyOperationForAllSupportedActions() {
        val ctx = getAuditActionContext();
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(List.of(mock), List.of("TEST.*"), List.of());
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
    }

    @Test
    void verifyOperationForUnmatchedActions() {
        val ctx = getAuditActionContext();
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(List.of(mock), List.of("PASSED.*"), List.of());
        mgr.record(ctx);
        assertTrue(mock.getAuditRecords().isEmpty());
    }

    @Test
    void verifyAuditRecordsSinceDate() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS",
            LocalDateTime.now(ZoneOffset.UTC).plusDays(1),
            new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
        val mock = new MockAuditTrailManager();
        val mgr = new FilterAndDelegateAuditTrailManager(List.of(mock), List.of("TEST.*"), List.of());
        mgr.record(ctx);
        assertFalse(mock.getAuditRecords().isEmpty());
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(AuditTrailManager.WhereClauseFields.DATE, LocalDateTime.now(ZoneOffset.UTC));
        assertEquals(1, mgr.getAuditRecords(criteria).size());
        assertDoesNotThrow(mgr::removeAll);
    }
}
