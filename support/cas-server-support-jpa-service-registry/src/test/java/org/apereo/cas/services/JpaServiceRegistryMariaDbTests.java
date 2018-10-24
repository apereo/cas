package org.apereo.cas.services;

import org.apereo.cas.category.MariaDbCategory;
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
@TestPropertySource(locations = "classpath:svcregmariadb.properties")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 3306)
@Category(MariaDbCategory.class)
public class JpaServiceRegistryMariaDbTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryMariaDbTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
