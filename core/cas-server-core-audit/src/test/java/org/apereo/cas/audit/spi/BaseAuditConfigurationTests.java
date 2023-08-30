package org.apereo.cas.audit.spi;

import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseAuditConfigurationTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SuppressWarnings("JavaUtilDate")
public abstract class BaseAuditConfigurationTests {
    private static final String USER = RandomUtils.randomAlphanumeric(6);

    protected AuditActionContext auditActionContext;

    public abstract AuditTrailManager getAuditTrailManager();

    @BeforeEach
    public void onSetUp() {
        val auditTrailManager = getAuditTrailManager();
        auditTrailManager.removeAll();
        this.auditActionContext = new AuditActionContext(USER, "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()), new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
        auditTrailManager.record(auditActionContext);
    }

    @Test
    void verifyAuditByDate() throws Throwable {
        val time = LocalDate.now(ZoneOffset.UTC).minusDays(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(AuditTrailManager.WhereClauseFields.DATE, time);
        val results = getAuditTrailManager().getAuditRecords(criteria);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyAuditByPrincipal() throws Throwable {
        val time = LocalDate.now(ZoneOffset.UTC).minusDays(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, time,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, USER);
        val results = getAuditTrailManager().getAuditRecords(criteria);
        assertFalse(results.isEmpty());
    }
}
