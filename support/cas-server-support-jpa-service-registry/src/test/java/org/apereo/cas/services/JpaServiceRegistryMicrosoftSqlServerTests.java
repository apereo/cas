package org.apereo.cas.services;

import org.apereo.cas.category.MsSqlServerCategory;
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
@TestPropertySource(locations = "classpath:svcregsqlserver.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 1433)
@Category(MsSqlServerCategory.class)
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryMicrosoftSqlServerTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
