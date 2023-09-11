package org.apereo.cas.support.events.jpa;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaEventsConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test cases for {@link JpaCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    JpaEventsConfiguration.class,
    CasHibernateJpaConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = "cas.jdbc.show-sql=false")
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Getter
@Tag("JDBC")
class JpaCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;

    
    @Override
    @Transactional
    @Test
    protected void verifyLoadOps() throws Throwable {
        super.verifyLoadOps();
    }

    @Override
    @Transactional
    @Test
    protected void verifySave() throws Throwable {
        super.verifySave();
    }
}
