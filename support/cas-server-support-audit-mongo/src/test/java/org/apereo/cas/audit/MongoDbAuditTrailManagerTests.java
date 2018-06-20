package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.DateTimeUtils;
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
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

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
@TestPropertySource(locations = {"classpath:/mongoaudit.properties"})
@Category(MongoDbCategory.class)
@Slf4j
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
        final var twoDaysAgo = LocalDate.now().minusDays(2);
        final var since = DateTimeUtils.dateOf(twoDaysAgo);
        final var ctx = new AuditActionContext("casuser", "resource",
            "action", "appcode", since, "clientIp",
            "serverIp");
        auditTrailExecutionPlan.record(ctx);

        final Set results = auditTrailExecutionPlan.getAuditRecordsSince(twoDaysAgo);
        assertFalse(results.isEmpty());
    }
}
