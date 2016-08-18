package org.apereo.cas.support.events.mongo;

import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/mongo-cloudtest-eventscontext.xml")
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    private CasEventRepository repository;

    @Override
    public CasEventRepository getRepositoryInstance() {
        return this.repository;
    }
}
