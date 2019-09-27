package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentMongoDbConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
},
    properties = {
        "cas.consent.mongo.host=localhost",
        "cas.consent.mongo.port=27017",
        "cas.consent.mongo.userId=root",
        "cas.consent.mongo.password=secret",
        "cas.consent.mongo.authenticationDatabaseName=admin",
        "cas.consent.mongo.dropCollection=true",
        "cas.consent.mongo.databaseName=consent"
    })
@Tag("MongoDb")
@Getter
@EnabledIfContinuousIntegration
public class MongoDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
