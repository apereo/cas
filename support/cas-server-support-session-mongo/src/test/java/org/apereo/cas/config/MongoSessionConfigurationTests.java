package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link MongoSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasMongoSessionAutoConfiguration.class, properties = {
    "spring.session.store-type=MONGODB",
    "spring.session.mongodb.collection-name=MongoDbSessionRepository",

    "spring.data.mongodb.host=localhost",
    "spring.data.mongodb.port=27017",
    "spring.data.mongodb.database=sessions",
    "spring.data.mongodb.username=root",
    "spring.data.mongodb.password=secret"
})
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class MongoSessionConfigurationTests {
    @Test
    void verifyOperation() {
    }
}
