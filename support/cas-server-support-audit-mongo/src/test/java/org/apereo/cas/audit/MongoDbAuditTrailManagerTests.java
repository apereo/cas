package org.apereo.cas.audit;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CasCoreAuditConfiguration.class,
        CasSupportMongoDbAuditConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class})
@TestPropertySource(locations = {"classpath:/mongoaudit.properties"})
@Slf4j
public class MongoDbAuditTrailManagerTests {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private AuditTrailExecutionPlan auditTrailManager;

    @Test
    public void verify() {
        final Date since = DateTimeUtils.dateOf(LocalDate.now().minusDays(2));
        final AuditActionContext ctx = new AuditActionContext("casuser", "resource",
            "action", "appcode", since, "clientIp",
            "serverIp");
        auditTrailManager.record(ctx);

        final Set results = auditTrailManager.getAuditRecordsSince(LocalDate.now());
        assertFalse(results.isEmpty());
    }
}
