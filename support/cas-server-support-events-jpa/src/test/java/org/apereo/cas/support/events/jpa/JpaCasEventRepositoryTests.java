package org.apereo.cas.support.events.jpa;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaEventsAutoConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Test cases for {@link JpaCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasJpaEventsAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class
},
    properties = "cas.jdbc.show-sql=false")
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Getter
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class JpaCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;
}
