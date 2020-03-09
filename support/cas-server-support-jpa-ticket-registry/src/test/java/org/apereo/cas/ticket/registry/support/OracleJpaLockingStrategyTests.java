package org.apereo.cas.ticket.registry.support;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link JpaLockingStrategy}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.jpa.user=system",
    "cas.ticket.registry.jpa.password=Oradoc_db1",
    "cas.ticket.registry.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.ticket.registry.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class OracleJpaLockingStrategyTests extends JpaLockingStrategyTests {
}
