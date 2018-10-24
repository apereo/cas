package org.apereo.cas.services;

import org.apereo.cas.category.MySQLCategory;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(locations = "classpath:svcregmysql.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 3306)
@Category(MySQLCategory.class)
public class JpaServiceRegistryMySQLTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryMySQLTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
