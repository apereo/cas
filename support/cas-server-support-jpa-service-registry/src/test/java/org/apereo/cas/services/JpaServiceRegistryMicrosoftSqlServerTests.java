package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.jpa.driver-class=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.service-registry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=services;encrypt=false;trustServerCertificate=true",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.SQLServerDialect"
})
@EnabledIfListeningOnPort(port = 1433)
@Tag("MsSqlServer")
class JpaServiceRegistryMicrosoftSqlServerTests extends JpaServiceRegistryTests {
}
