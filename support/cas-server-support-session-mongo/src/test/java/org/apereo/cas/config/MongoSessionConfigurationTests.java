package org.apereo.cas.config;
import module java.base;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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

    "spring.mongodb.authentication-database=admin",
    "spring.mongodb.host=localhost",
    "spring.mongodb.port=27017",
    "spring.mongodb.database=sessions",
    "spring.mongodb.username=root",
    "spring.mongodb.password=secret"
})
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class MongoSessionConfigurationTests {
    @Test
    void verifyOperation() {
    }
}
