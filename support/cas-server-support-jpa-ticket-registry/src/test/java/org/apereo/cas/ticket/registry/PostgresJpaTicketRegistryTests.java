package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.jpa.user=postgres",
    "cas.ticket.registry.jpa.password=password",
    "cas.ticket.registry.jpa.driver-class=org.postgresql.Driver",
    "cas.ticket.registry.jpa.url=jdbc:postgresql://localhost:5432/tickets",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
class PostgresJpaTicketRegistryTests extends BaseJpaTicketRegistryTests {
}
