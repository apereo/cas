package org.apereo.cas.support.events.mongo;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.MongoDbEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    MongoDbEventsConfiguration.class,
    CasCoreHttpConfiguration.class,
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
@EnabledIfPortOpen(port = 27017)
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
}
