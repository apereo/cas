package org.apereo.cas.services;

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
    "cas.service-registry.jpa.user=sa",
    "cas.service-registry.jpa.password=p@ssw0rd",
    "cas.service-registry.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.service-registry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=services",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
@EnabledIfPortOpen(port = 1433)
@Tag("MsSqlServer")
public class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
}
