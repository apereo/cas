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
    "cas.jdbc.show-sql=true",
    "cas.service-registry.jpa.ddl-auto=create",
    "cas.service-registry.jpa.user=system",
    "cas.service-registry.jpa.password=Oradoc_db1",
    "cas.service-registry.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.service-registry.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class JpaServiceRegistryOracleTests extends JpaServiceRegistryTests {
}
