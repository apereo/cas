package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasSupportCouchDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchDbAuditTrailManagerTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportCouchDbAuditConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class
},
    properties = {
        "cas.audit.couch-db.asynchronous=false",
        "cas.audit.couch-db.username=cas",
        "cas.audit.couch-db.caching=false",
        "cas.audit.couch-db.password=password"
    })
@Tag("CouchDb")
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private AuditActionContextCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("couchDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired
    @Qualifier("auditCouchDbFactory")
    private CouchDbConnectorFactory auditCouchDbFactory;

    @BeforeEach
    public void setUp() {
        auditCouchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(auditCouchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        auditCouchDbFactory.getCouchDbInstance().deleteDatabase(auditCouchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}
