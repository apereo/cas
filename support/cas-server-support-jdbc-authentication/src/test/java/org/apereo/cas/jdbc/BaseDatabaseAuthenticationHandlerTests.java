package org.apereo.cas.jdbc;

import module java.base;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.DatabaseAuthenticationTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasHibernateJpaAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseDatabaseAuthenticationHandlerTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
