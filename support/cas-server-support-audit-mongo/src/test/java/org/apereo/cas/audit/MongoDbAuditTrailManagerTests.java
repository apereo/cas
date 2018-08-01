package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(
    classes = {
        CasCoreAuditConfiguration.class,
        CasSupportMongoDbAuditConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class})
@TestPropertySource(properties = {
    "cas.audit.mongo.host=localhost",
    "cas.audit.mongo.port=8081",
    "cas.audit.mongo.dropCollection=true",
    "cas.audit.mongo.asynchronous=false",
    "cas.audit.mongo.databaseName=audit"
    })
@Category(MongoDbCategory.class)
public class MongoDbAuditTrailManagerTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private AuditTrailExecutionPlan auditTrailExecutionPlan;

    @Test
    public void verify() {
        val twoDaysAgo = LocalDate.now().minusDays(2);
        val since = DateTimeUtils.dateOf(twoDaysAgo);
        val ctx = new AuditActionContext("casuser", "resource",
            "action", "appcode", since, "clientIp",
            "serverIp");
        auditTrailExecutionPlan.record(ctx);

        val results = auditTrailExecutionPlan.getAuditRecordsSince(twoDaysAgo);
        assertFalse(results.isEmpty());
    }
}
