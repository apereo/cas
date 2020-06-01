package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link MongoDbAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportMongoDbAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class
},
    properties = {
        "cas.audit.mongo.host=localhost",
        "cas.audit.mongo.port=27017",
        "cas.audit.mongo.dropCollection=true",
        "cas.audit.mongo.asynchronous=false",
        "cas.audit.mongo.user-id=root",
        "cas.audit.mongo.password=secret",
        "cas.audit.mongo.database-name=audit",
        "cas.audit.mongo.authentication-database-name=admin"
    })
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
public class MongoDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("mongoDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
