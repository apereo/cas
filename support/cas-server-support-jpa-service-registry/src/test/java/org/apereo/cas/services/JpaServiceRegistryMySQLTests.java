package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.MySQLCategory;
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
@TestPropertySource(locations = "classpath:svcregmysql.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 3306)
@Category(MySQLCategory.class)
public class JpaServiceRegistryMySQLTests extends JpaServiceRegistryTests {
    public JpaServiceRegistryMySQLTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }
}
