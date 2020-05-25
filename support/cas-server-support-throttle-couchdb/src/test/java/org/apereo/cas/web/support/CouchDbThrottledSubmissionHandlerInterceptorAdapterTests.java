package org.apereo.cas.web.support;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasCouchDbThrottlingConfiguration;
import org.apereo.cas.config.CasSupportCouchDbAuditConfiguration;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link CouchDbThrottledSubmissionHandlerInterceptorAdapterTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    CasCouchDbThrottlingConfiguration.class,
    CasSupportCouchDbAuditConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.audit.couch-db.db-name=throttle",
        "cas.audit.couch-db.asynchronous=false",
        "cas.audit.couch-db.username=cas",
        "cas.audit.couch-db.password=password",
        "cas.authn.throttle.username-parameter=username",
        "cas.authn.throttle.failure.range-seconds=5"
    })
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbThrottledSubmissionHandlerInterceptorAdapterTests extends
    BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private AuditActionContextCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("auditCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}
