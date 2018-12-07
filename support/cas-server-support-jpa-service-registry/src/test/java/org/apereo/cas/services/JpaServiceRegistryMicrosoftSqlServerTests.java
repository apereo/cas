package org.apereo.cas.services;

import org.apereo.cas.util.test.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.test.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
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
@Tag("mssqlserver")
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
}
