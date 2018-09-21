package org.apereo.cas.support.events.mongo;

import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.MongoDbEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {MongoDbEventsConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(properties = {
    "cas.events.mongo.userId=root",
    "cas.events.mongo.password=secret",
    "cas.events.mongo.host=localhost",
    "cas.events.mongo.port=27017",
    "cas.events.mongo.authenticationDatabaseName=admin",
    "cas.events.mongo.databaseName=events",
    "cas.events.mongo.dropCollection=true"
    })
@Getter
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
}
