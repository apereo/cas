package org.apereo.cas.audit;

import org.apereo.cas.audit.config.CasSupportJdbcAuditConfiguration;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.junit.ConditionalSpringRunner;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * This is {@link CasSupportJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(ConditionalSpringRunner.class)
@SpringBootTest(
    classes = {
        CasCoreAuditConfiguration.class,
        CasSupportJdbcAuditConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = "cas.audit.jdbc.asynchronous=false")
public class CasSupportJdbcAuditConfigurationTests {

    @Autowired
    @Qualifier("jdbcAuditTrailManager")
    private AuditTrailManager jdbcAuditTrailManager;

    @Test
    public void verifyAuditManager() {
        val time = LocalDate.now().minusDays(2);
        val since = DateTimeUtils.dateOf(time);
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", since, "1.2.3.4",
            "1.2.3.4");
        jdbcAuditTrailManager.record(ctx);
        val results = jdbcAuditTrailManager.getAuditRecordsSince(time);
        assertFalse(results.isEmpty());
    }
}
