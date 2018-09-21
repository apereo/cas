package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportMongoDbAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

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
    "cas.audit.mongo.port=27017",
    "cas.audit.mongo.dropCollection=true",
    "cas.audit.mongo.asynchronous=false",
    "cas.audit.mongo.userId=root",
    "cas.audit.mongo.password=secret",
    "cas.audit.mongo.databaseName=audit",
    "cas.audit.mongo.authenticationDatabaseName=admin"
})
@Category(MongoDbCategory.class)
@Getter
public class MongoDbAuditTrailManagerTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("mongoDbAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
