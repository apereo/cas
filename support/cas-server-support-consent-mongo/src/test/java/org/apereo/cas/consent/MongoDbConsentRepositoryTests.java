package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentMongoDbConfiguration;

import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MongoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentMongoDbConfiguration.class,
    CasConsentCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class
})
@Category(MongoDbCategory.class)
@TestPropertySource(properties = {
    "cas.consent.mongo.host=localhost",
    "cas.consent.mongo.port=27017",
    "cas.consent.mongo.userId=root",
    "cas.consent.mongo.password=secret",
    "cas.consent.mongo.authenticationDatabaseName=admin",
    "cas.consent.mongo.dropCollection=true",
    "cas.consent.mongo.databaseName=consent"
    })
public class MongoDbConsentRepositoryTests extends BaseConsentRepositoryTests {
}
