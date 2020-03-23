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
    "cas.ticket.registry.jpa.user=root",
    "cas.ticket.registry.jpa.password=password",
    "cas.ticket.registry.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.ticket.registry.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJpaTicketRegistryTests extends JpaTicketRegistryTests {
}
