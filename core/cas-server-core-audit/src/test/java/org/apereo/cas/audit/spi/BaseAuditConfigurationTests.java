package org.apereo.cas.audit.spi;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
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

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    public abstract AuditTrailManager getAuditTrailManager();

    @BeforeEach
    public void onSetUp() {
        val auditTrailManager = getAuditTrailManager();
        auditTrailManager.removeAll();
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London")
            .setExtraInfo(Map.of("Hello", "World"))
            .setHeaders(Map.of("H1", "V1"));
        this.auditActionContext = new AuditActionContext(USER, "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()), clientInfo);
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
