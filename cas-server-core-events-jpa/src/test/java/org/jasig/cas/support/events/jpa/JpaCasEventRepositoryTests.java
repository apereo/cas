package org.jasig.cas.support.events.jpa;

import org.jasig.cas.support.events.AbstractCasEventRepositoryTests;
import org.jasig.cas.support.events.dao.CasEventRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test cases for {@link JpaCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations={"classpath:/jpa-eventscontext-test.xml"})
public class JpaCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    private CasEventRepository repository;

    @Override
    public CasEventRepository getRepositoryInstance() {
        return this.repository;
    }
}
