package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
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
public class OracleJpaTicketRegistryTests extends JpaTicketRegistryTests {
}
