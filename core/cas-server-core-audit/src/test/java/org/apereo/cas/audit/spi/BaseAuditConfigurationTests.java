package org.apereo.cas.audit.spi;

import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
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
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCookieConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

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
