package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

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
@Tag("TicketRegistryTestWithoutEncryption")
class PostgresJpaTicketRegistryTests extends BaseJpaTicketRegistryTests {

    @RepeatedTest(1)
    void verifyAttributesAreIndexedJsonb() {
        val jdbcTemplate = new JdbcTemplate(dataSourceTicket);
        val columnType = jdbcTemplate.queryForObject("SELECT data_type FROM information_schema.columns "
            + "WHERE table_name = 'cas_tickets' AND column_name = 'attributes'", String.class);
        assertEquals("jsonb", columnType);
        jpaTicketRegistryTransactionTemplate.executeWithoutResult(_ -> jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_ticket_attributes ON cas_tickets USING GIN (attributes jsonb_path_ops)"));
        val indexDefinition = jdbcTemplate.queryForObject("SELECT indexdef FROM pg_indexes "
            + "WHERE tablename = 'cas_tickets' AND indexname = 'idx_ticket_attributes'", String.class);
        assertTrue(Strings.CI.contains(indexDefinition, "gin"), indexDefinition);
        assertTrue(Strings.CS.contains(indexDefinition, "jsonb_path_ops"), indexDefinition);
    }
}
