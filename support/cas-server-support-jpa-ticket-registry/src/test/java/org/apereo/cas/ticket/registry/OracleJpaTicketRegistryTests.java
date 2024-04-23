package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.jpa.ddl-auto=create-drop",
    "cas.ticket.registry.jpa.user=system",
    "cas.ticket.registry.jpa.password=Oradoc_db1",
    "cas.ticket.registry.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.ticket.registry.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.OracleDialect"
})
@EnabledIfListeningOnPort(port = 1521)
@Tag("Oracle")
class OracleJpaTicketRegistryTests extends BaseJpaTicketRegistryTests {
}
