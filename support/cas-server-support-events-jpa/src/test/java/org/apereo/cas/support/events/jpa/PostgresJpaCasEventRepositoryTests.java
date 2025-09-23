package org.apereo.cas.support.events.jpa;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJpaCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.events.jpa.ddl-auto=create-drop",
    "cas.events.jpa.user=postgres",
    "cas.events.jpa.password=password",
    "cas.events.jpa.driver-class=org.postgresql.Driver",
    "cas.events.jpa.url=jdbc:postgresql://localhost:5432/events",
    "cas.events.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class PostgresJpaCasEventRepositoryTests extends JpaCasEventRepositoryTests {
}
