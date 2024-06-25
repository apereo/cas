package org.apereo.cas.jdbc;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.DatabaseAuthenticationTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseDatabaseAuthenticationHandlerTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
