package org.apereo.cas.audit.spi;

import module java.base;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseAuditConfigurationTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseAuditConfigurationTests {
    private static final String USER = RandomUtils.randomAlphanumeric(6);

    protected AuditActionContext auditActionContext;

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreEventsAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }

    public abstract AuditTrailManager getAuditTrailManager();

    @BeforeEach
    void onSetUp() {
        val auditTrailManager = getAuditTrailManager();
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London")
            .setExtraInfo(Map.of("Hello", "World"))
            .setHeaders(Map.of("H1", "V1"));
        this.auditActionContext = new AuditActionContext(USER, "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()), clientInfo);
        auditTrailManager.record(auditActionContext);
    }

    @RetryingTest(value = 3, suspendForMs = 3000)
    void verifyAuditByDate() {
        val time = LocalDateTime.now(ZoneOffset.UTC).minusDays(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(AuditTrailManager.WhereClauseFields.DATE, time);
        val results = getAuditTrailManager().getAuditRecords(criteria);
        assertFalse(results.isEmpty());
    }

    @RetryingTest(value = 3, suspendForMs = 3000)
    void verifyAuditByRecentDate() {
        val time = auditActionContext.getWhenActionWasPerformed().minusMinutes(1);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, time,
            AuditTrailManager.WhereClauseFields.COUNT, 100L,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, USER);
        val results = getAuditTrailManager().getAuditRecords(criteria);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().noneMatch(result -> result.getWhenActionWasPerformed().isBefore(time)));
    }

    @Test
    void verifyAuditByPrincipal() {
        val time = LocalDateTime.now(ZoneOffset.UTC).minusDays(2);
        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, time,
            AuditTrailManager.WhereClauseFields.COUNT, 10L,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, USER);
        val results = getAuditTrailManager().getAuditRecords(criteria);
        assertFalse(results.isEmpty());
    }

    @RetryingTest(value = 3, suspendForMs = 3000)
    void verifyAuditSummary() {
        val action = "SUMMARY_" + RandomUtils.randomAlphanumeric(8).toUpperCase(Locale.ENGLISH);
        val now = LocalDateTime.now(Clock.systemUTC());
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London");
        getAuditTrailManager().record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo));
        getAuditTrailManager().record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo));
        val query = new AuditSummaryQuery(now.minusHours(2), AuditSummaryQuery.hourOf(now).plusHours(1), Set.of(action, "UNKNOWN"));
        val counts = getAuditTrailManager().summarize(query).orElseThrow();
        assertEquals(2, counts.stream().filter(count -> action.equals(count.action())).mapToLong(AuditActionCount::count).sum());
        assertTrue(counts.stream().allMatch(count -> count.hour().equals(AuditSummaryQuery.hourOf(now))));
        assertTrue(counts.stream().noneMatch(count -> "UNKNOWN".equals(count.action())));
    }

    @RetryingTest(value = 3, suspendForMs = 3000)
    void verifyAuditSummaryByTarget() {
        val action = "SUMMARY_" + RandomUtils.randomAlphanumeric(8).toUpperCase(Locale.ENGLISH);
        val now = LocalDateTime.now(Clock.systemUTC());
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London");
        val manager = getAuditTrailManager();
        manager.record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo, "1001"));
        manager.record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo, "1001"));
        manager.record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo, "mfa-duo"));
        manager.record(new AuditActionContext(USER, "TEST", action, "CAS", now, clientInfo));
        val query = new AuditSummaryQuery(now.minusHours(2), AuditSummaryQuery.hourOf(now).plusHours(1), Set.of(action), true);
        val counts = manager.summarize(query).orElseThrow().stream()
            .filter(count -> action.equals(count.action()))
            .collect(Collectors.groupingBy(count -> String.valueOf(count.target()), Collectors.summingLong(AuditActionCount::count)));
        assertEquals(Map.of("1001", 2L, "mfa-duo", 1L), counts);

        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, now.minusMinutes(1),
            AuditTrailManager.WhereClauseFields.COUNT, 100L,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, USER);
        assertTrue(manager.getAuditRecords(criteria).stream()
            .anyMatch(record -> action.equals(record.getActionPerformed()) && "mfa-duo".equals(record.getTarget())));
    }
}
