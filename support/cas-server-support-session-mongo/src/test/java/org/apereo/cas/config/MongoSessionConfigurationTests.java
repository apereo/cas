package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link MongoSessionConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MongoSessionConfiguration.class
}, properties = {
    "spring.session.store-type=MONGODB",
    "spring.session.mongodb.collection-name=MongoDbSessionRepository",

    "spring.data.mongodb.host=localhost",
    "spring.data.mongodb.port=27017",
    "spring.data.mongodb.database=sessions",
    "spring.data.mongodb.username=root",
    "spring.data.mongodb.password=secret"
})
@Tag("MongoDb")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoSessionConfigurationTests {
    @Test
    public void verifyOperation() throws Exception {
    }
}
