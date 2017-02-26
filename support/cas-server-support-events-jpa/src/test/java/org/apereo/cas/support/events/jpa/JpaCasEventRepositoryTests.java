package org.apereo.cas.support.events.jpa;

import org.apereo.cas.config.JpaEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Test cases for {@link JpaCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JpaEventsConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class JpaCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    private CasEventRepository repository;

    @Override
    public CasEventRepository getRepositoryInstance() {
        return this.repository;
    }
}
