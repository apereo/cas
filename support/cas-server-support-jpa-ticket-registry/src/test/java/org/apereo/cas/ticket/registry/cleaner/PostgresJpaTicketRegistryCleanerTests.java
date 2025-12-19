package org.apereo.cas.ticket.registry.cleaner;

import module java.base;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link TicketRegistryCleaner} for postgres.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
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
class PostgresJpaTicketRegistryCleanerTests extends BaseJpaTicketRegistryCleanerTests {
}
