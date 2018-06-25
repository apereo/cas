package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.MsSqlServerCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@Slf4j
@TestPropertySource(locations = "classpath:svcregsqlserver.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 1433)
@Category(MsSqlServerCategory.class)
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryMicrosoftSqlServerTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
