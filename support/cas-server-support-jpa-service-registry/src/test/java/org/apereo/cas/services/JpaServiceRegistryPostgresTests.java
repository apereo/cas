package org.apereo.cas.services;

import org.apereo.cas.category.PostgresCategory;
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
@TestPropertySource(locations = "classpath:svcregpostgres.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 5432)
@Category(PostgresCategory.class)
public class JpaServiceRegistryPostgresTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryPostgresTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
