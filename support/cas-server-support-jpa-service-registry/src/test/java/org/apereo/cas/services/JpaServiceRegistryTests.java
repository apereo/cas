package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreServicesConfiguration.class
})
@Tag("JDBC")
@DirtiesContext
public class JpaServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("jpaServiceRegistry")
    protected ServiceRegistry serviceRegistry;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
