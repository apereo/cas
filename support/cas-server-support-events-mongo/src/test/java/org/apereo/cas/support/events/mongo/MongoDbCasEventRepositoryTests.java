package org.apereo.cas.support.events.mongo;

import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.MongoDbEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Category(MongoDbCategory.class)
@SpringBootTest(classes = {MongoDbEventsConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(properties = {
    "cas.events.mongo.host=localhost",
    "cas.events.mongo.port=8081",
    "cas.events.mongo.databaseName=events",
    "cas.events.mongo.dropCollection=true"
    })
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Override
    public CasEventRepository getRepositoryInstance() {
        return this.casEventRepository;
    }
}
