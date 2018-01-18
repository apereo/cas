package org.apereo.cas.audit;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.config.CasSupportJdbcAuditConfiguration;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link CasSupportJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CasCoreAuditConfiguration.class,
        CasSupportJdbcAuditConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = "cas.audit.jdbc.asynchronous=false")
@Slf4j
public class CasSupportJdbcAuditConfigurationTests {

    @Autowired
    @Qualifier("jdbcAuditTrailManager")
    private AuditTrailManager jdbcAuditTrailManager;

    @Test
    public void verifyAuditManager() {
        final Date since = DateTimeUtils.dateOf(LocalDate.now().minusDays(2));
        final AuditActionContext ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", since, "1.2.3.4",
            "1.2.3.4");
        jdbcAuditTrailManager.record(ctx);
        final Set results = jdbcAuditTrailManager.getAuditRecordsSince(LocalDate.now());
        assertFalse(results.isEmpty());
    }
}
