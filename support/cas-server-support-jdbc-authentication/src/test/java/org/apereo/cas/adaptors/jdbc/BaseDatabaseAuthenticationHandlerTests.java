package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.DatabaseAuthenticationTestConfiguration;
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
    CasHibernateJpaConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
public abstract class BaseDatabaseAuthenticationHandlerTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
