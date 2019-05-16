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
@TestPropertySource(properties = {
    "cas.serviceRegistry.jpa.user=sa",
    "cas.serviceRegistry.jpa.password=p@ssw0rd",
    "cas.serviceRegistry.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.serviceRegistry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=services",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 1433)
@Tag("MsSqlServer")
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
}
