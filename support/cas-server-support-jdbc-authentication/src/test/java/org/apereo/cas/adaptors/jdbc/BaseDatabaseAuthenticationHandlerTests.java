package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.DatabaseAuthenticationTestConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;

/**
 * This is {@link BaseDatabaseAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasHibernateJpaConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@DirtiesContext
public abstract class BaseDatabaseAuthenticationHandlerTests {
}
