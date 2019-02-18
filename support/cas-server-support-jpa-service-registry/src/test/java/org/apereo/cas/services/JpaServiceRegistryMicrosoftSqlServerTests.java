package org.apereo.cas.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
@Tag("MsSqlServer")
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
}
