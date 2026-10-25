package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.ticket.registry.jpa.ddl-auto=update",
    "cas.ticket.registry.jpa.user=root",
    "cas.ticket.registry.jpa.password=password",
    "cas.ticket.registry.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.ticket.registry.jpa.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@ResourceLock("mySqlTicketRegistryTables")
@Tag("MySQL")
@Tag("TicketRegistryTestWithoutEncryption")
class MySQLJpaTicketRegistryTests extends BaseJpaTicketRegistryTests {

    @RepeatedTest(1)
    void verifyTicketTableIndexesAreCreated() {
        val indexes = new JdbcTemplate(dataSourceTicket).queryForList(
            "SELECT DISTINCT LOWER(INDEX_NAME) FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'cas_tickets'", String.class);
        assertTrue(indexes.containsAll(List.of("idx_ticket_principal", "idx_ticket_parent", "idx_type_principal")),
            indexes::toString);
    }
}
