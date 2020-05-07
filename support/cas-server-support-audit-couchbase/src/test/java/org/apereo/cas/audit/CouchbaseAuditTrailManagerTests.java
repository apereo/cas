package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportCouchbaseAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchbaseAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportCouchbaseAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class
}, properties = {
    "cas.audit.couchbase.bucket=testbucket",
    "cas.audit.couchbase.clusterUsername=admin",
    "cas.audit.couchbase.clusterPassword=password",
    "cas.audit.couchbase.asynchronous=false"
})
@Tag("Couchbase")
@Getter
@EnabledIfPortOpen(port = 8091)
public class CouchbaseAuditTrailManagerTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("couchbaseAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired
    @Qualifier("auditsCouchbaseClientFactory")
    private CouchbaseClientFactory auditsCouchbaseClientFactory;
}
