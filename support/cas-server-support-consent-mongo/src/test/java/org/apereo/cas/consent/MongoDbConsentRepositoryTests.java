package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentMongoDbConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link MongoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentMongoDbConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.consent.mongo.host=localhost",
        "cas.consent.mongo.port=27017",
        "cas.consent.mongo.user-id=root",
        "cas.consent.mongo.password=secret",
        "cas.consent.mongo.authentication-database-name=admin",
        "cas.consent.mongo.drop-collection=true",
        "cas.consent.mongo.database-name=consent"
    })
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
public class MongoDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
