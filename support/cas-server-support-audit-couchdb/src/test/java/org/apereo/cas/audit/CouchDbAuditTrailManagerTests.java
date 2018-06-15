package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasSupportCouchDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.couchdb.AuditActionContextCouchDbRepository;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * This is {@link CouchDbAuditTrailManagerTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(
    classes = {
        CasCoreAuditConfiguration.class,
        CasSupportCouchDbAuditConfiguration.class,
        CasCouchDbCoreConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class
    },
    properties = {"cas.audit.couchDb.asyncronous=false"})
@Category(CouchDbCategory.class)
public class CouchDbAuditTrailManagerTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private AuditActionContextCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private AuditTrailExecutionPlan auditTrailExecutionPlan;

    @Autowired
    @Qualifier("auditCouchDbConnector")
    private CouchDbConnector couchDbConnector;

    @Autowired
    @Qualifier("auditCouchDbInstance")
    private CouchDbInstance couchDbInstance;

    @Before
    public void setUp() {
        couchDbInstance.createDatabaseIfNotExists(couchDbConnector.getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        couchDbInstance.deleteDatabase(couchDbConnector.getDatabaseName());
    }

    @Test
    public void verify() {
        val since = LocalDate.now().minusDays(2);
        val ctx = new AuditActionContext("casuser", "resource",
            "action", "appcode", DateTimeUtils.dateOf(LocalDate.now()), "clientIp",
            "serverIp");
        auditTrailExecutionPlan.record(ctx);

        val results = auditTrailExecutionPlan.getAuditRecordsSince(since);
        assertEquals(1, results.size());
    }
}
