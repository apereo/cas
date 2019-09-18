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
    "cas.serviceRegistry.jpa.user=system",
    "cas.serviceRegistry.jpa.password=Oradoc_db1",
    "cas.serviceRegistry.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.serviceRegistry.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class JpaServiceRegistryOracleTests extends JpaServiceRegistryTests {
}
