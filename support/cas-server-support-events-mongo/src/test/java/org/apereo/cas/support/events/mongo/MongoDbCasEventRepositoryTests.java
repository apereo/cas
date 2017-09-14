package org.apereo.cas.support.events.mongo;

import org.apereo.cas.config.MongoDbEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MongoDbEventsConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/mongoevents.properties"})
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Override
    public CasEventRepository getRepositoryInstance() {
        return this.casEventRepository;
    }
}
