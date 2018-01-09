package org.apereo.cas.audit;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * This is {@link MongoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CasSupportMongoDbAuditConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class})
@TestPropertySource(locations = {"classpath:/mongoaudit.properties"})
public class MongoDbAuditTrailManagerTests {

    @Autowired
    @Qualifier("auditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Test
    public void verify() {
        final AuditActionContext ctx = new AuditActionContext("casuser", "resource",
            "action", "appcode", new Date(), "clientIp",
            "serverIp");
        auditTrailManager.record(ctx);
    }

}
