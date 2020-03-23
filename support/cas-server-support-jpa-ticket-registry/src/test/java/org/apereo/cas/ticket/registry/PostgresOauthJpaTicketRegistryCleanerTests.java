package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link PostgresOauthJpaTicketRegistryCleanerTests} handles test cases for {@link DefaultTicketRegistryCleaner}.
 *
 * @author charlibot
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.jpa.user=postgres",
    "cas.ticket.registry.jpa.password=password",
    "cas.ticket.registry.jpa.driverClass=org.postgresql.Driver",
    "cas.ticket.registry.jpa.url=jdbc:postgresql://localhost:5432/tickets",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class PostgresOauthJpaTicketRegistryCleanerTests extends OAuthJpaTicketRegistryCleanerTests {
}
