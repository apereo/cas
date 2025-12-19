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
    "cas.ticket.registry.jpa.user=root",
    "cas.ticket.registry.jpa.password=password",
    "cas.ticket.registry.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.ticket.registry.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class MySQLJpaTicketRegistryCleanerTests extends BaseJpaTicketRegistryCleanerTests {
}
