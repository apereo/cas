package org.apereo.cas.support.events.mongo;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMongoDbEventsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("MongoDb")
@SpringBootTest(classes = {
    CasMongoDbEventsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.events.mongo.user-id=root",
        "cas.events.mongo.password=secret",
        "cas.events.mongo.host=localhost",
        "cas.events.mongo.port=27017",
        "cas.events.mongo.authentication-database-name=admin",
        "cas.events.mongo.database-name=events",
        "cas.events.mongo.drop-collection=true"
    })
@Getter
@EnabledIfListeningOnPort(port = 27017)
@EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;
}
